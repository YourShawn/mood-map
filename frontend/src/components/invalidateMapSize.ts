import type { Map as LeafletMap } from "leaflet";

/** Recalculate Leaflet panes after container/layout changes so tiles stay visible. */
export function invalidateMapSize(map: LeafletMap): void {
  map.invalidateSize({ animate: false, pan: false });
}
