import { useCallback, useState } from "react";
import type { LatLng } from "../api/types";

export type GeoStatus = "idle" | "pending" | "granted" | "denied" | "unavailable";

export function useGeolocation() {
  const [position, setPosition] = useState<LatLng | null>(null);
  const [status, setStatus] = useState<GeoStatus>("idle");

  const request = useCallback(() => {
    if (!navigator.geolocation) {
      setStatus("unavailable");
      return;
    }
    setStatus("pending");
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setPosition({ lat: pos.coords.latitude, lng: pos.coords.longitude });
        setStatus("granted");
      },
      () => {
        setStatus("denied");
      },
      { enableHighAccuracy: true, timeout: 12000, maximumAge: 30_000 }
    );
  }, []);

  const setManual = useCallback((lat: number, lng: number) => {
    setPosition({ lat, lng });
    if (status === "idle" || status === "denied" || status === "unavailable" || status === "pending") {
      setStatus("granted");
    }
  }, [status]);

  return { position, status, request, setManual };
}
