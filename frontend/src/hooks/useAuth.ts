import { useCallback, useEffect, useState } from "react";
import { api, getToken, setToken } from "../api/client";
import type { UserProfile } from "../api/types";

export function useAuth() {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [ready, setReady] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const ensureSession = useCallback(async () => {
    try {
      setError(null);
      if (!getToken()) {
        const session = await api.createSession();
        setToken(session.token);
      }
      const me = await api.me();
      setProfile(me);
    } catch {
      setToken(null);
      try {
        const session = await api.createSession();
        setToken(session.token);
        const me = await api.me();
        setProfile(me);
      } catch (err) {
        setError(err instanceof Error ? err.message : "auth failed");
      }
    } finally {
      setReady(true);
    }
  }, []);

  useEffect(() => {
    void ensureSession();
  }, [ensureSession]);

  const saveNickname = useCallback(async (nickname: string | null) => {
    const session = await api.updateNickname(nickname);
    setToken(session.token);
    setProfile((prev) =>
      prev
        ? { ...prev, nickname: session.nickname }
        : { userId: session.userId, nickname: session.nickname, createdAt: new Date().toISOString() }
    );
  }, []);

  return { profile, ready, error, saveNickname, ensureSession };
}
