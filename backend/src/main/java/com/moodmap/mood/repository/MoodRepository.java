package com.moodmap.mood.repository;

import com.moodmap.mood.entity.Mood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MoodRepository extends JpaRepository<Mood, String> {

    @Query("""
            SELECT m FROM Mood m JOIN FETCH m.user
            WHERE m.expiresAt > :now
              AND m.latitude BETWEEN :minLat AND :maxLat
              AND m.longitude BETWEEN :minLng AND :maxLng
            ORDER BY m.createdAt DESC
            """)
    List<Mood> findActiveInBox(
            @Param("now") Instant now,
            @Param("minLat") java.math.BigDecimal minLat,
            @Param("maxLat") java.math.BigDecimal maxLat,
            @Param("minLng") java.math.BigDecimal minLng,
            @Param("maxLng") java.math.BigDecimal maxLng
    );

    @Query("""
            SELECT m FROM Mood m JOIN FETCH m.user
            WHERE m.expiresAt > :now
              AND m.latitude BETWEEN :minLat AND :maxLat
              AND (m.longitude >= :minLng OR m.longitude <= :maxLng)
            ORDER BY m.createdAt DESC
            """)
    List<Mood> findActiveInBoxCrossingAntimeridian(
            @Param("now") Instant now,
            @Param("minLat") java.math.BigDecimal minLat,
            @Param("maxLat") java.math.BigDecimal maxLat,
            @Param("minLng") java.math.BigDecimal minLng,
            @Param("maxLng") java.math.BigDecimal maxLng
    );

    Optional<Mood> findFirstByUser_IdOrderByCreatedAtDesc(String userId);

    List<Mood> findByUser_IdAndExpiresAtAfterOrderByCreatedAtDesc(String userId, Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Mood m WHERE m.expiresAt <= :now")
    int deleteExpired(@Param("now") Instant now);
}
