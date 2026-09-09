package com.moodmap.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Jwt jwt = new Jwt();
    private final Cors cors = new Cors();
    private final Mood mood = new Mood();

    public Jwt getJwt() {
        return jwt;
    }

    public Cors getCors() {
        return cors;
    }

    public Mood getMood() {
        return mood;
    }

    public static class Jwt {
        private String secret = "dev-only-change-me-32-chars-min";
        private long ttlHours = 720;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getTtlHours() {
            return ttlHours;
        }

        public void setTtlHours(long ttlHours) {
            this.ttlHours = ttlHours;
        }
    }

    public static class Cors {
        private String origins = "http://localhost:5173";

        public String getOrigins() {
            return origins;
        }

        public void setOrigins(String origins) {
            this.origins = origins;
        }

        public List<String> originList() {
            return Arrays.stream(origins.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
    }

    public static class Mood {
        private long ttlHours = 24;
        private double defaultRadiusKm = 5;
        private double maxRadiusKm = 50;
        private int nearbyLimit = 200;
        private int minIntervalSeconds = 10;
        private int privacyDecimals = 3;

        public long getTtlHours() {
            return ttlHours;
        }

        public void setTtlHours(long ttlHours) {
            this.ttlHours = ttlHours;
        }

        public double getDefaultRadiusKm() {
            return defaultRadiusKm;
        }

        public void setDefaultRadiusKm(double defaultRadiusKm) {
            this.defaultRadiusKm = defaultRadiusKm;
        }

        public double getMaxRadiusKm() {
            return maxRadiusKm;
        }

        public void setMaxRadiusKm(double maxRadiusKm) {
            this.maxRadiusKm = maxRadiusKm;
        }

        public int getNearbyLimit() {
            return nearbyLimit;
        }

        public void setNearbyLimit(int nearbyLimit) {
            this.nearbyLimit = nearbyLimit;
        }

        public int getMinIntervalSeconds() {
            return minIntervalSeconds;
        }

        public void setMinIntervalSeconds(int minIntervalSeconds) {
            this.minIntervalSeconds = minIntervalSeconds;
        }

        public int getPrivacyDecimals() {
            return privacyDecimals;
        }

        public void setPrivacyDecimals(int privacyDecimals) {
            this.privacyDecimals = privacyDecimals;
        }
    }
}
