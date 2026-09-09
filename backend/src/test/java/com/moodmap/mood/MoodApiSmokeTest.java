package com.moodmap.mood;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodmap.auth.entity.User;
import com.moodmap.auth.repository.UserRepository;
import com.moodmap.mood.entity.Mood;
import com.moodmap.mood.entity.MoodType;
import com.moodmap.mood.repository.MoodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MoodApiSmokeTest {

    private static final String HERE = """
            {"moodType":"CALM","note":"午后阳光","latitude":31.2304,"longitude":121.4737}
            """;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MoodRepository moodRepository;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void clean() {
        moodRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void dropNearbyAndFadeLifecycle() throws Exception {
        mockMvc.perform(post("/api/moods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(HERE))
                .andExpect(status().isUnauthorized());

        String tokenA = sessionToken();
        String tokenB = sessionToken();

        MvcResult created = mockMvc.perform(post("/api/moods")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(HERE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.moodType").value("CALM"))
                .andExpect(jsonPath("$.note").value("午后阳光"))
                .andExpect(jsonPath("$.mine").value(true))
                .andExpect(jsonPath("$.fade").value(1.0))
                .andReturn();

        String moodId = objectMapper.readTree(created.getResponse().getContentAsByteArray()).get("id").asText();

        mockMvc.perform(post("/api/moods")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"moodType\":\"HAPPY\",\"latitude\":999,\"longitude\":0}"))
                .andExpect(status().isBadRequest());

        JsonNode nearby = objectMapper.readTree(mockMvc.perform(get("/api/moods/nearby")
                        .header("Authorization", "Bearer " + tokenB)
                        .param("lat", "31.2304")
                        .param("lng", "121.4737")
                        .param("radiusKm", "5"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray());

        assertThat(nearby).hasSize(1);
        assertThat(nearby.get(0).get("id").asText()).isEqualTo(moodId);
        assertThat(nearby.get(0).get("mine").asBoolean()).isFalse();
        // privacy rounding to 3 decimals
        assertThat(nearby.get(0).get("latitude").asDouble()).isEqualTo(31.230);

        mockMvc.perform(get("/api/moods/nearby")
                        .header("Authorization", "Bearer " + tokenB)
                        .param("lat", "39.9042")
                        .param("lng", "116.4074")
                        .param("radiusKm", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        User other = userRepository.save(new User(UUID.randomUUID().toString(), Instant.now()));
        Instant past = Instant.now().minus(2, ChronoUnit.HOURS);
        moodRepository.save(new Mood(
                UUID.randomUUID().toString(),
                other,
                MoodType.SAD,
                "gone",
                new BigDecimal("31.2304000"),
                new BigDecimal("121.4737000"),
                past.minus(24, ChronoUnit.HOURS),
                past
        ));

        mockMvc.perform(get("/api/moods/nearby")
                        .header("Authorization", "Bearer " + tokenA)
                        .param("lat", "31.2304")
                        .param("lng", "121.4737"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(delete("/api/moods/" + moodId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/moods/" + moodId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/moods/me")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private String sessionToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/session"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).get("token").asText();
    }
}
