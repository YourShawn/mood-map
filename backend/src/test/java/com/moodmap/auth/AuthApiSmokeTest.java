package com.moodmap.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodmap.auth.dto.SessionResponse;
import com.moodmap.auth.repository.UserRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthApiSmokeTest {

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
    void anonymousSessionAndNickname() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/auth/session"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.userId").isNotEmpty())
                .andReturn();

        SessionResponse session = objectMapper.readValue(created.getResponse().getContentAsByteArray(), SessionResponse.class);
        assertThat(session.nickname()).isNull();

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(session.userId()));

        mockMvc.perform(patch("/api/auth/me")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"小枫\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("小枫"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
}
