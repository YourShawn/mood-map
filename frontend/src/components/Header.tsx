import type { Locale } from "../i18n";
import { t } from "../i18n";

interface Props {
  locale: Locale;
  nickname: string | null;
  onToggleLocale: () => void;
  onOpenProfile: () => void;
  onLocate: () => void;
  locating: boolean;
}

export function Header({
  locale,
  nickname,
  onToggleLocale,
  onOpenProfile,
  onLocate,
  locating,
}: Props) {
  return (
    <header className="top-bar">
      <div className="brand">
        <span className="mark" aria-hidden>
          ◎
        </span>
        <div>
          <h1>{t(locale, "title")}</h1>
          <p>{t(locale, "subtitle")}</p>
        </div>
      </div>
      <div className="actions">
        <button type="button" className="chip" onClick={onLocate} disabled={locating}>
          {locating ? t(locale, "locating") : t(locale, "locate")}
        </button>
        <button type="button" className="chip" onClick={onOpenProfile}>
          {nickname || t(locale, "anon")}
        </button>
        <button type="button" className="chip ghost" onClick={onToggleLocale}>
          {t(locale, "lang")}
        </button>
      </div>
    </header>
  );
}
