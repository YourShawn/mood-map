package com.moodmap.auth.service;

import com.moodmap.auth.dto.SessionResponse;
import com.moodmap.auth.dto.UserResponse;
import com.moodmap.auth.entity.User;
import com.moodmap.auth.repository.UserRepository;
import com.moodmap.common.exception.ApiException;
import com.moodmap.common.security.JwtService;
import com.moodmap.common.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern NICKNAME = Pattern.compile("^[\\p{L}\\p{N}_ \\-·]{1,32}$");

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public SessionResponse createAnonymousSession() {
        Instant now = Instant.now();
        User user = new User(UUID.randomUUID().toString(), now);
        userRepository.save(user);
        String token = jwtService.issue(user.getId(), null);
        return new SessionResponse(token, user.getId(), null);
    }

    @Transactional(readOnly = true)
    public UserResponse me(UserPrincipal principal) {
        User user = requireUser(principal.userId());
        return toResponse(user);
    }

    @Transactional
    public SessionResponse updateNickname(UserPrincipal principal, String rawNickname) {
        User user = requireUser(principal.userId());
        String nickname = normalizeNickname(rawNickname);
        user.setNickname(nickname);
        user.setLastSeenAt(Instant.now());
        userRepository.save(user);
        String token = jwtService.issue(user.getId(), user.getNickname());
        return new SessionResponse(token, user.getId(), user.getNickname());
    }

    private User requireUser(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ApiException.unauthorized("Session is no longer valid"));
    }

    private String normalizeNickname(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim();
        if (!NICKNAME.matcher(trimmed).matches()) {
            throw ApiException.badRequest("invalid_nickname", "Nickname may use letters, numbers, spaces, _ - ·");
        }
        return trimmed;
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getNickname(), user.getCreatedAt());
    }
}
