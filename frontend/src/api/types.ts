export type MoodType =
  | "HAPPY"
  | "CALM"
  | "SAD"
  | "ANGRY"
  | "ANXIOUS"
  | "EXCITED"
  | "TIRED"
  | "GRATEFUL"
  | "LOVED"
  | "OKAY";

export interface Session {
  token: string;
  userId: string;
  nickname: string | null;
}

export interface UserProfile {
  userId: string;
  nickname: string | null;
  createdAt: string;
}

export interface MoodPin {
  id: string;
  moodType: MoodType;
  note: string | null;
  latitude: number;
  longitude: number;
  nickname: string | null;
  createdAt: string;
  expiresAt: string;
  fade: number;
  mine: boolean;
}

export interface LatLng {
  lat: number;
  lng: number;
}
