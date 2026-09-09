package com.moodmap.mood.entity;

import com.moodmap.auth.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "moods")
public class Mood {

    @Id
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "mood_type", nullable = false, length = 32)
    private MoodType moodType;

    @Column(length = 140)
    private String note;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected Mood() {
    }

    public Mood(
            String id,
            User user,
            MoodType moodType,
            String note,
            BigDecimal latitude,
            BigDecimal longitude,
            Instant createdAt,
            Instant expiresAt
    ) {
        this.id = id;
        this.user = user;
        this.moodType = moodType;
        this.note = note;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public MoodType getMoodType() {
        return moodType;
    }

    public String getNote() {
        return note;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
