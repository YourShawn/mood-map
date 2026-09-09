package com.moodmap.auth.dto;

public record SessionResponse(
        String token,
        String userId,
        String nickname
) {
}
