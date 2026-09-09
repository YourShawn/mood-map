import type { MoodType } from "../api/types";

export interface MoodMeta {
  type: MoodType;
  emoji: string;
  color: string;
}

export const MOODS: MoodMeta[] = [
  { type: "HAPPY", emoji: "😊", color: "#F4D35E" },
  { type: "CALM", emoji: "😌", color: "#81B29A" },
  { type: "SAD", emoji: "😢", color: "#7B8CDE" },
  { type: "ANGRY", emoji: "😤", color: "#E07A5F" },
  { type: "ANXIOUS", emoji: "😰", color: "#9B8EC4" },
  { type: "EXCITED", emoji: "🤩", color: "#F28482" },
  { type: "TIRED", emoji: "😴", color: "#8D99AE" },
  { type: "GRATEFUL", emoji: "🙏", color: "#E9C46A" },
  { type: "LOVED", emoji: "🥰", color: "#E5989B" },
  { type: "OKAY", emoji: "😐", color: "#B8C0C8" },
];

const byType = Object.fromEntries(MOODS.map((m) => [m.type, m])) as Record<MoodType, MoodMeta>;

export function moodMeta(type: MoodType): MoodMeta {
  return byType[type] ?? { type, emoji: "•", color: "#B8C0C8" };
}

export function pinOpacity(fade: number): number {
  return 0.28 + 0.72 * Math.max(0, Math.min(1, fade));
}

export function relativeTime(iso: string, locale: string): string {
  const delta = Date.now() - new Date(iso).getTime();
  const minutes = Math.max(0, Math.round(delta / 60000));
  const rtf = new Intl.RelativeTimeFormat(locale, { numeric: "auto" });
  if (minutes < 60) {
    return rtf.format(-minutes, "minute");
  }
  const hours = Math.round(minutes / 60);
  return rtf.format(-hours, "hour");
}
