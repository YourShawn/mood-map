# Database schema — 心情地图 / Mood Map

MySQL 8. Flyway owns DDL (`backend/src/main/resources/db/migration`). Hibernate `ddl-auto` is `validate`.

## ER

```
users 1──────< moods
```

Anonymous visitors are first-class rows in `users`. Nickname is optional. There are no passwords, emails, or social profiles.

## `users`

| Column | Type | Notes |
| --- | --- | --- |
| `id` | `VARCHAR(36)` | PK, UUID |
| `nickname` | `VARCHAR(32)` | Optional display name |
| `created_at` | `TIMESTAMP(3)` | UTC |
| `last_seen_at` | `TIMESTAMP(3)` | Updated on session / profile writes |

## `moods`

| Column | Type | Notes |
| --- | --- | --- |
| `id` | `VARCHAR(36)` | PK, UUID |
| `user_id` | `VARCHAR(36)` | FK → `users.id` `ON DELETE CASCADE` |
| `mood_type` | `VARCHAR(32)` | See enum below |
| `note` | `VARCHAR(140)` | Optional short note |
| `latitude` | `DECIMAL(10,7)` | WGS84, CHECK ±90 |
| `longitude` | `DECIMAL(10,7)` | WGS84, CHECK ±180 |
| `created_at` | `TIMESTAMP(3)` | UTC |
| `expires_at` | `TIMESTAMP(3)` | `created_at + MOOD_TTL_HOURS` (default 24h) |

### Indexes

- `PRIMARY KEY (id)` on both tables
- `fk_moods_user` foreign key
- `idx_moods_active_geo (expires_at, latitude, longitude)` — nearby active pins
- `idx_moods_user_created (user_id, created_at)` — own history / rate limit

### Mood types

`happy`, `calm`, `sad`, `angry`, `anxious`, `excited`, `tired`, `grateful`, `loved`, `okay`

## Fade / TTL

Reads only return rows with `expires_at > now()`. A scheduled job deletes expired rows so storage stays small. The API also returns a `fade` ratio (`remaining / ttl`) so the map can dim older pins.

## Privacy

Other people's coordinates are rounded to 3 decimal degrees (~100 m) in API responses. Own pins keep full precision.
