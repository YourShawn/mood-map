import L from "leaflet";
import { useEffect, useMemo } from "react";
import { MapContainer, Marker, Popup, TileLayer, useMap, useMapEvents } from "react-leaflet";
import type { MoodPin } from "../api/types";
import type { Locale } from "../i18n";
import { t } from "../i18n";
import { moodMeta, pinOpacity, relativeTime } from "../lib/moods";

interface Props {
  center: { lat: number; lng: number } | null;
  pins: MoodPin[];
  locale: Locale;
  onMapClick: (lat: number, lng: number) => void;
  onDelete: (id: string) => void;
  followToken: number;
}

function FollowCenter({
  center,
  token,
}: {
  center: { lat: number; lng: number };
  token: number;
}) {
  const map = useMap();
  useEffect(() => {
    map.flyTo([center.lat, center.lng], Math.max(map.getZoom(), 14), { duration: 0.7 });
  }, [center.lat, center.lng, token, map]);
  return null;
}

function ClickCapture({ onMapClick }: { onMapClick: (lat: number, lng: number) => void }) {
  useMapEvents({
    click(event) {
      onMapClick(event.latlng.lat, event.latlng.lng);
    },
  });
  return null;
}

function moodIcon(pin: MoodPin): L.DivIcon {
  const meta = moodMeta(pin.moodType);
  const opacity = pinOpacity(pin.fade);
  const mine = pin.mine ? " mine" : "";
  return L.divIcon({
    className: "mood-marker",
    iconSize: [40, 40],
    iconAnchor: [20, 20],
    popupAnchor: [0, -16],
    html: `<div class="mood-marker-disk${mine}" style="--mood:${meta.color};opacity:${opacity}">${meta.emoji}</div>`,
  });
}

export function MoodMap({ center, pins, locale, onMapClick, onDelete, followToken }: Props) {
  const fallback = { lat: 31.2304, lng: 121.4737 };
  const view = center ?? fallback;
  const localeTag = locale === "zh" ? "zh-CN" : "en";

  const markers = useMemo(
    () =>
      pins.map((pin) => (
        <Marker key={pin.id} position={[pin.latitude, pin.longitude]} icon={moodIcon(pin)}>
          <Popup>
            <div className="pin-popup">
              <strong>
                {moodMeta(pin.moodType).emoji} {t(locale, pin.moodType)}
              </strong>
              <span className="muted">
                {pin.mine ? t(locale, "you") : pin.nickname || t(locale, "anon")} ·{" "}
                {relativeTime(pin.createdAt, localeTag)}
              </span>
              {pin.note ? <p>{pin.note}</p> : null}
              {pin.mine ? (
                <button type="button" className="linkish" onClick={() => onDelete(pin.id)}>
                  {t(locale, "delete")}
                </button>
              ) : null}
            </div>
          </Popup>
        </Marker>
      )),
    [pins, locale, localeTag, onDelete]
  );

  return (
    <MapContainer
      center={[view.lat, view.lng]}
      zoom={center ? 14 : 3}
      className="mood-map"
      zoomControl={false}
      attributionControl
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> &copy; <a href="https://carto.com/attributions">CARTO</a>'
        url="https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png"
      />
      <ClickCapture onMapClick={onMapClick} />
      {center ? <FollowCenter center={center} token={followToken} /> : null}
      {markers}
    </MapContainer>
  );
}
