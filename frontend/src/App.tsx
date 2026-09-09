import { useEffect, useState } from "react";
import { Header } from "./components/Header";
import { MoodMap } from "./components/MoodMap";
import { MoodPicker } from "./components/MoodPicker";
import { NicknameDialog } from "./components/NicknameDialog";
import { useAuth } from "./hooks/useAuth";
import { useGeolocation } from "./hooks/useGeolocation";
import { useMoods } from "./hooks/useMoods";
import { detectLocale, t, type Locale } from "./i18n";
import type { MoodType } from "./api/types";

export default function App() {
  const [locale, setLocale] = useState<Locale>(() => detectLocale());
  const [note, setNote] = useState("");
  const [toast, setToast] = useState<string | null>(null);
  const [profileOpen, setProfileOpen] = useState(false);
  const [followToken, setFollowToken] = useState(0);
  const [busy, setBusy] = useState(false);

  const { profile, ready, saveNickname } = useAuth();
  const geo = useGeolocation();
  const moods = useMoods(geo.position);

  useEffect(() => {
    document.documentElement.lang = locale === "zh" ? "zh-CN" : "en";
    localStorage.setItem("moodmap.locale", locale);
  }, [locale]);

  useEffect(() => {
    geo.request();
    // first load only
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (!toast) {
      return;
    }
    const id = window.setTimeout(() => setToast(null), 2400);
    return () => window.clearTimeout(id);
  }, [toast]);

  function toggleLocale() {
    setLocale((prev) => (prev === "zh" ? "en" : "zh"));
  }

  async function onPick(type: MoodType) {
    if (!geo.position || busy) {
      setToast(t(locale, "tapMap"));
      return;
    }
    setBusy(true);
    try {
      await moods.drop(type, note, geo.position);
      setNote("");
      setToast(t(locale, "dropped"));
    } catch (err) {
      const status = (err as { status?: number }).status;
      setToast(status === 429 ? t(locale, "wait") : t(locale, "error"));
    } finally {
      setBusy(false);
    }
  }

  const banner =
    geo.status === "pending"
      ? t(locale, "locating")
      : geo.status === "denied" || geo.status === "unavailable"
        ? t(locale, "locationDenied")
        : !geo.position
          ? t(locale, "tapMap")
          : moods.pins.length === 0 && !moods.loading
            ? t(locale, "nearbyEmpty")
            : null;

  return (
    <div className="app">
      <MoodMap
        center={geo.position}
        pins={moods.pins}
        locale={locale}
        followToken={followToken}
        onMapClick={(lat, lng) => {
          geo.setManual(lat, lng);
          setFollowToken((n) => n + 1);
        }}
        onDelete={(id) => {
          void moods.remove(id).catch(() => setToast(t(locale, "error")));
        }}
      />

      <Header
        locale={locale}
        nickname={profile?.nickname ?? null}
        locating={geo.status === "pending"}
        onToggleLocale={toggleLocale}
        onOpenProfile={() => setProfileOpen(true)}
        onLocate={() => {
          geo.request();
          setFollowToken((n) => n + 1);
        }}
      />

      <section className="composer">
        {banner ? <p className="banner">{banner}</p> : null}
        <p className="hint">{t(locale, "dropHint")}</p>
        <MoodPicker locale={locale} disabled={!ready || busy} onPick={onPick} />
        <input
          className="note"
          maxLength={140}
          value={note}
          placeholder={t(locale, "notePlaceholder")}
          onChange={(e) => setNote(e.target.value)}
        />
      </section>

      {profileOpen ? (
        <NicknameDialog
          locale={locale}
          initial={profile?.nickname ?? ""}
          onClose={() => setProfileOpen(false)}
          onSave={saveNickname}
        />
      ) : null}

      {toast ? <div className="toast">{toast}</div> : null}
    </div>
  );
}
