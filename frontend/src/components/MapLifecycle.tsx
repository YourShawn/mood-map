import { useEffect } from "react";
import { useMap } from "react-leaflet";
import { invalidateMapSize } from "./invalidateMapSize";

function scheduleInvalidate(map: ReturnType<typeof useMap>): () => void {
  invalidateMapSize(map);
  const frame = window.requestAnimationFrame(() => invalidateMapSize(map));
  return () => window.cancelAnimationFrame(frame);
}

/**
 * Keeps Leaflet in sync with container size after pins, follow pans, and resizes.
 * Without this, tiles often go blank after the second placement.
 */
export function MapLifecycle({
  pinCount,
  followToken,
}: {
  pinCount: number;
  followToken: number;
}) {
  const map = useMap();

  useEffect(() => scheduleInvalidate(map), [map, pinCount, followToken]);

  useEffect(() => {
    const onResize = () => invalidateMapSize(map);
    window.addEventListener("resize", onResize);

    const container = map.getContainer();
    const observer =
      typeof ResizeObserver === "undefined" ? null : new ResizeObserver(() => invalidateMapSize(map));
    observer?.observe(container);

    return () => {
      window.removeEventListener("resize", onResize);
      observer?.disconnect();
    };
  }, [map]);

  return null;
}

export function FollowCenter({
  center,
  token,
}: {
  center: { lat: number; lng: number };
  token: number;
}) {
  const map = useMap();

  useEffect(() => {
    const onMoveEnd = () => invalidateMapSize(map);
    map.once("moveend", onMoveEnd);
    map.flyTo([center.lat, center.lng], Math.max(map.getZoom(), 14), { duration: 0.7 });
    invalidateMapSize(map);
    return () => {
      map.off("moveend", onMoveEnd);
    };
  }, [center.lat, center.lng, token, map]);

  return null;
}
