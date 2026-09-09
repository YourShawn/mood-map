package com.moodmap.common;

import com.moodmap.common.geo.GeoUtils;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class GeoUtilsTest {

    @Test
    void haversineKnownDistance() {
        // Roughly 1.1 km between these nearby Shanghai points
        double km = GeoUtils.haversineKm(31.2304, 121.4737, 31.2404, 121.4737);
        assertThat(km).isCloseTo(1.11, within(0.05));
    }

    @Test
    void boundingBoxContainsCenter() {
        GeoUtils.BoundingBox box = GeoUtils.boundingBox(31.23, 121.47, 5);
        assertThat(box.minLat()).isLessThan(31.23);
        assertThat(box.maxLat()).isGreaterThan(31.23);
        assertThat(box.minLng()).isLessThan(121.47);
        assertThat(box.maxLng()).isGreaterThan(121.47);
        assertThat(box.crossesAntimeridian()).isFalse();
    }

    @Test
    void privacyRound() {
        assertThat(GeoUtils.roundForPrivacy(31.2304567, 3)).isEqualTo(31.230);
    }

    @Test
    void fadeGoesFromOneToZero() {
        Instant created = Instant.parse("2026-01-01T00:00:00Z");
        Instant expires = created.plus(Duration.ofHours(24));
        assertThat(GeoUtils.fade(created, expires, created)).isEqualTo(1.0);
        assertThat(GeoUtils.fade(created, expires, created.plus(Duration.ofHours(12))))
                .isCloseTo(0.5, within(0.001));
        assertThat(GeoUtils.fade(created, expires, expires)).isEqualTo(0.0);
        assertThat(GeoUtils.fade(created, expires, expires.plusSeconds(1))).isEqualTo(0.0);
    }
}
