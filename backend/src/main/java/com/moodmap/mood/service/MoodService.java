package com.moodmap.mood.service;

import com.moodmap.auth.entity.User;
import com.moodmap.auth.repository.UserRepository;
import com.moodmap.common.config.AppProperties;
import com.moodmap.common.exception.ApiException;
import com.moodmap.common.geo.GeoUtils;
import com.moodmap.common.security.UserPrincipal;
import com.moodmap.mood.dto.CreateMoodRequest;
import com.moodmap.mood.dto.MoodResponse;
import com.moodmap.mood.entity.Mood;
import com.moodmap.mood.repository.MoodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class MoodService {

    private final MoodRepository moodRepository;
    private final UserRepository userRepository;
    private final AppProperties appProperties;

    public MoodService(
            MoodRepository moodRepository,
            UserRepository userRepository,
            AppProperties appProperties
    ) {
        this.moodRepository = moodRepository;
        this.userRepository = userRepository;
        this.appProperties = appProperties;
    }

    @Transactional
    public MoodResponse create(UserPrincipal principal, CreateMoodRequest request) {
        User user = userRepository.findById(principal.userId())
                .orElseThrow(() -> ApiException.unauthorized("Session is no longer valid"));

        Instant now = Instant.now();
        int minInterval = appProperties.getMood().getMinIntervalSeconds();
        if (minInterval > 0) {
            moodRepository.findFirstByUser_IdOrderByCreatedAtDesc(user.getId())
                    .ifPresent(last -> {
                        if (Duration.between(last.getCreatedAt(), now).getSeconds() < minInterval) {
                            throw ApiException.tooMany("Please wait a moment before dropping another mood");
                        }
                    });
        }

        String note = sanitizeNote(request.note());
        Instant expiresAt = now.plus(Duration.ofHours(appProperties.getMood().getTtlHours()));
        Mood mood = new Mood(
                UUID.randomUUID().toString(),
                user,
                request.moodType(),
                note,
                scale(request.latitude()),
                scale(request.longitude()),
                now,
                expiresAt
        );
        moodRepository.save(mood);
        user.setLastSeenAt(now);
        userRepository.save(user);
        return toResponse(mood, user.getId(), now, false);
    }

    @Transactional(readOnly = true)
    public List<MoodResponse> nearby(UserPrincipal principal, double lat, double lng, Double radiusKm) {
        AppProperties.Mood cfg = appProperties.getMood();
        double radius = radiusKm == null ? cfg.getDefaultRadiusKm() : radiusKm;
        if (radius <= 0 || radius > cfg.getMaxRadiusKm()) {
            throw ApiException.badRequest("invalid_radius", "Radius must be between 0 and " + cfg.getMaxRadiusKm() + " km");
        }
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw ApiException.badRequest("invalid_location", "Latitude/longitude out of range");
        }

        Instant now = Instant.now();
        GeoUtils.BoundingBox box = GeoUtils.boundingBox(lat, lng, radius);
        List<Mood> candidates = box.crossesAntimeridian()
                ? moodRepository.findActiveInBoxCrossingAntimeridian(
                        now,
                        BigDecimal.valueOf(box.minLat()),
                        BigDecimal.valueOf(box.maxLat()),
                        BigDecimal.valueOf(box.minLng()),
                        BigDecimal.valueOf(box.maxLng())
                )
                : moodRepository.findActiveInBox(
                        now,
                        BigDecimal.valueOf(box.minLat()),
                        BigDecimal.valueOf(box.maxLat()),
                        BigDecimal.valueOf(box.minLng()),
                        BigDecimal.valueOf(box.maxLng())
                );

        String viewerId = principal.userId();
        return candidates.stream()
                .filter(m -> GeoUtils.haversineKm(
                        lat, lng,
                        m.getLatitude().doubleValue(),
                        m.getLongitude().doubleValue()
                ) <= radius)
                .sorted(Comparator.comparing(Mood::getCreatedAt).reversed())
                .limit(cfg.getNearbyLimit())
                .map(m -> toResponse(m, viewerId, now, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MoodResponse> mine(UserPrincipal principal) {
        Instant now = Instant.now();
        return moodRepository.findByUser_IdAndExpiresAtAfterOrderByCreatedAtDesc(principal.userId(), now)
                .stream()
                .map(m -> toResponse(m, principal.userId(), now, false))
                .toList();
    }

    @Transactional
    public void delete(UserPrincipal principal, String moodId) {
        Mood mood = moodRepository.findById(moodId)
                .orElseThrow(() -> ApiException.notFound("Mood not found"));
        if (!mood.getUser().getId().equals(principal.userId())) {
            throw ApiException.forbidden("You can only remove your own mood");
        }
        moodRepository.delete(mood);
    }

    @Transactional
    public int purgeExpired() {
        return moodRepository.deleteExpired(Instant.now());
    }

    private MoodResponse toResponse(Mood mood, String viewerId, Instant now, boolean privacyRound) {
        boolean mine = mood.getUser().getId().equals(viewerId);
        double lat = mood.getLatitude().doubleValue();
        double lng = mood.getLongitude().doubleValue();
        if (privacyRound && !mine) {
            int decimals = appProperties.getMood().getPrivacyDecimals();
            lat = GeoUtils.roundForPrivacy(lat, decimals);
            lng = GeoUtils.roundForPrivacy(lng, decimals);
        }
        return new MoodResponse(
                mood.getId(),
                mood.getMoodType(),
                mood.getNote(),
                lat,
                lng,
                mood.getUser().getNickname(),
                mood.getCreatedAt(),
                mood.getExpiresAt(),
                GeoUtils.fade(mood.getCreatedAt(), mood.getExpiresAt(), now),
                mine
        );
    }

    private static String sanitizeNote(String note) {
        if (note == null || note.isBlank()) {
            return null;
        }
        String cleaned = note.replaceAll("<[^>]*>", "").trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private static BigDecimal scale(double value) {
        return BigDecimal.valueOf(value).setScale(7, RoundingMode.HALF_UP);
    }
}
