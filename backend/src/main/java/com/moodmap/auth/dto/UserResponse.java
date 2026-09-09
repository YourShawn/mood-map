package com.moodmap.auth.dto;

import java.time.Instant;

public record UserResponse(
        String userId,
        String nickname,
        Instant createdAt
) {
}
