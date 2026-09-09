import type { MoodPin, MoodType, Session, UserProfile } from "./types";

const TOKEN_KEY = "moodmap.token";

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string | null): void {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    localStorage.removeItem(TOKEN_KEY);
  }
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = getToken();
  const headers = new Headers(init.headers);
  if (!headers.has("Content-Type") && init.body) {
    headers.set("Content-Type", "application/json");
  }
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }
  const response = await fetch(path, { ...init, headers });
  if (response.status === 204) {
    return undefined as T;
  }
  const text = await response.text();
  const data = text ? JSON.parse(text) : null;
  if (!response.ok) {
    const message = data?.message ?? `HTTP ${response.status}`;
    const error = new Error(message) as Error & { status?: number; code?: string };
    error.status = response.status;
    error.code = data?.code;
    throw error;
  }
  return data as T;
}

export const api = {
  createSession(): Promise<Session> {
    return request("/api/auth/session", { method: "POST" });
  },
  me(): Promise<UserProfile> {
    return request("/api/auth/me");
  },
  updateNickname(nickname: string | null): Promise<Session> {
    return request("/api/auth/me", {
      method: "PATCH",
      body: JSON.stringify({ nickname }),
    });
  },
  createMood(body: {
    moodType: MoodType;
    note?: string;
    latitude: number;
    longitude: number;
  }): Promise<MoodPin> {
    return request("/api/moods", {
      method: "POST",
      body: JSON.stringify(body),
    });
  },
  nearby(lat: number, lng: number, radiusKm = 5): Promise<MoodPin[]> {
    const params = new URLSearchParams({
      lat: String(lat),
      lng: String(lng),
      radiusKm: String(radiusKm),
    });
    return request(`/api/moods/nearby?${params.toString()}`);
  },
  mine(): Promise<MoodPin[]> {
    return request("/api/moods/me");
  },
  deleteMood(id: string): Promise<void> {
    return request(`/api/moods/${id}`, { method: "DELETE" });
  },
};
