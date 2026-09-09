package com.moodmap.common.geo;

public final class GeoUtils {

    public static final double EARTH_RADIUS_KM = 6371.0;

    private GeoUtils() {
    }

    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.min(1.0, Math.sqrt(a)));
    }

    public static BoundingBox boundingBox(double lat, double lon, double radiusKm) {
        double latDelta = Math.toDegrees(radiusKm / EARTH_RADIUS_KM);
        double cosLat = Math.cos(Math.toRadians(lat));
        double lonDelta = Math.abs(cosLat) < 1e-6
                ? 180.0
                : Math.toDegrees(radiusKm / (EARTH_RADIUS_KM * cosLat));
        return new BoundingBox(
                clamp(lat - latDelta, -90, 90),
                clamp(lat + latDelta, -90, 90),
                wrapLon(lon - lonDelta),
                wrapLon(lon + lonDelta)
        );
    }

    public static double roundForPrivacy(double coord, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(coord * factor) / factor;
    }

    public static double fade(java.time.Instant createdAt, java.time.Instant expiresAt, java.time.Instant now) {
        if (now.isAfter(expiresAt) || !expiresAt.isAfter(createdAt)) {
            return 0.0;
        }
        if (!now.isAfter(createdAt)) {
            return 1.0;
        }
        double ttl = expiresAt.toEpochMilli() - createdAt.toEpochMilli();
        double remaining = expiresAt.toEpochMilli() - now.toEpochMilli();
        return Math.max(0.0, Math.min(1.0, remaining / ttl));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double wrapLon(double lon) {
        double wrapped = ((lon + 180) % 360 + 360) % 360 - 180;
        return wrapped == -180 ? 180 : wrapped;
    }

    public record BoundingBox(double minLat, double maxLat, double minLng, double maxLng) {
        public boolean crossesAntimeridian() {
            return minLng > maxLng;
        }
    }
}
