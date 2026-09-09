package com.moodmap.mood.controller;

import com.moodmap.common.security.UserPrincipal;
import com.moodmap.mood.dto.CreateMoodRequest;
import com.moodmap.mood.dto.MoodResponse;
import com.moodmap.mood.service.MoodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/moods")
@Tag(name = "mood")
@Validated
public class MoodController {

    private final MoodService moodService;

    public MoodController(MoodService moodService) {
        this.moodService = moodService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Drop a mood pin at the current location")
    public MoodResponse create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateMoodRequest request
    ) {
        return moodService.create(principal, request);
    }

    @GetMapping("/nearby")
    @Operation(summary = "Recent moods near a point (unexpired only)")
    public List<MoodResponse> nearby(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") double lat,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double lng,
            @RequestParam(required = false) Double radiusKm
    ) {
        return moodService.nearby(principal, lat, lng, radiusKm);
    }

    @GetMapping("/me")
    @Operation(summary = "Your still-visible moods")
    public List<MoodResponse> mine(@AuthenticationPrincipal UserPrincipal principal) {
        return moodService.mine(principal);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove your own mood")
    public void delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id
    ) {
        moodService.delete(principal, id);
    }
}
