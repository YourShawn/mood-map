package com.moodmap.mood.dto;

import com.moodmap.mood.entity.MoodType;

import java.time.Instant;

public record MoodResponse(
        String id,
        MoodType moodType,
        String note,
        double latitude,
        double longitude,
        String nickname,
        Instant createdAt,
        Instant expiresAt,
        double fade,
        boolean mine
) {
}
