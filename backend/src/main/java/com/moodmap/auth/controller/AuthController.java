package com.moodmap.auth.controller;

import com.moodmap.auth.dto.SessionResponse;
import com.moodmap.auth.dto.UpdateProfileRequest;
import com.moodmap.auth.dto.UserResponse;
import com.moodmap.auth.service.AuthService;
import com.moodmap.common.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/session")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an anonymous session")
    public SessionResponse session() {
        return authService.createAnonymousSession();
    }

    @GetMapping("/me")
    @Operation(summary = "Current visitor")
    public UserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return authService.me(principal);
    }

    @PatchMapping("/me")
    @Operation(summary = "Set or clear an optional nickname")
    public SessionResponse updateMe(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return authService.updateNickname(principal, request.nickname());
    }
}
