import type { CSSProperties } from "react";
import { MOODS } from "../lib/moods";
import type { MoodType } from "../api/types";
import type { Locale } from "../i18n";
import { t } from "../i18n";

interface Props {
  locale: Locale;
  disabled: boolean;
  onPick: (type: MoodType) => void;
}

export function MoodPicker({ locale, disabled, onPick }: Props) {
  return (
    <div className="mood-grid" role="list">
      {MOODS.map((mood) => (
        <button
          key={mood.type}
          type="button"
          className="mood-btn"
          disabled={disabled}
          style={{ "--mood": mood.color } as CSSProperties}
          onClick={() => onPick(mood.type)}
          aria-label={t(locale, mood.type)}
        >
          <span className="emoji">{mood.emoji}</span>
          <span className="label">{t(locale, mood.type)}</span>
        </button>
      ))}
    </div>
  );
}
