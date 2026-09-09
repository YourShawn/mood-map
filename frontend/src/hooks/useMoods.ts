import { useCallback, useEffect, useState } from "react";
import { api } from "../api/client";
import type { LatLng, MoodPin, MoodType } from "../api/types";

export function useMoods(origin: LatLng | null) {
  const [pins, setPins] = useState<MoodPin[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    if (!origin) {
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const nearby = await api.nearby(origin.lat, origin.lng);
      setPins(nearby);
    } catch (err) {
      setError(err instanceof Error ? err.message : "failed");
    } finally {
      setLoading(false);
    }
  }, [origin]);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const drop = useCallback(
    async (moodType: MoodType, note: string | undefined, at: LatLng) => {
      const pin = await api.createMood({
        moodType,
        note: note?.trim() ? note.trim() : undefined,
        latitude: at.lat,
        longitude: at.lng,
      });
      setPins((prev) => [pin, ...prev.filter((p) => p.id !== pin.id)]);
      return pin;
    },
    []
  );

  const remove = useCallback(async (id: string) => {
    await api.deleteMood(id);
    setPins((prev) => prev.filter((p) => p.id !== id));
  }, []);

  return { pins, loading, error, refresh, drop, remove };
}
