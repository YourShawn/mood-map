CREATE TABLE users (
    id VARCHAR(36) NOT NULL,
    nickname VARCHAR(32) NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    last_seen_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE moods (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    mood_type VARCHAR(32) NOT NULL,
    note VARCHAR(140) NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    expires_at TIMESTAMP(3) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_moods_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_moods_lat CHECK (latitude >= -90 AND latitude <= 90),
    CONSTRAINT chk_moods_lng CHECK (longitude >= -180 AND longitude <= 180),
    INDEX idx_moods_active_geo (expires_at, latitude, longitude),
    INDEX idx_moods_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
