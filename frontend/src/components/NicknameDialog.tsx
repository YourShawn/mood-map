import { FormEvent, useState } from "react";
import type { Locale } from "../i18n";
import { t } from "../i18n";

interface Props {
  locale: Locale;
  initial: string;
  onSave: (nickname: string | null) => Promise<void>;
  onClose: () => void;
}

export function NicknameDialog({ locale, initial, onSave, onClose }: Props) {
  const [value, setValue] = useState(initial);
  const [busy, setBusy] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    try {
      await onSave(value.trim() ? value.trim() : null);
      onClose();
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="modal-backdrop" onClick={onClose} role="presentation">
      <form className="modal" onClick={(e) => e.stopPropagation()} onSubmit={submit}>
        <h2>{t(locale, "nickname")}</h2>
        <input
          autoFocus
          maxLength={32}
          value={value}
          placeholder={t(locale, "nicknamePlaceholder")}
          onChange={(e) => setValue(e.target.value)}
        />
        <div className="modal-actions">
          <button type="button" className="chip ghost" onClick={onClose}>
            {t(locale, "cancel")}
          </button>
          <button type="submit" className="chip" disabled={busy}>
            {t(locale, "save")}
          </button>
        </div>
      </form>
    </div>
  );
}
