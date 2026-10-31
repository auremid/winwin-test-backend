package com.winwin.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "INTERNAL_TOKEN=test-internal-token")
@AutoConfigureMockMvc
class TransformControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rejectsMissingInternalToken() throws Exception {
        mockMvc.perform(post("/api/transform")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hello\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsInvalidInternalToken() throws Exception {
        mockMvc.perform(post("/api/transform")
                        .header("X-Internal-Token", "wrong-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hello\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void transformsTextWithValidInternalToken() throws Exception {
        mockMvc.perform(post("/api/transform")
                        .header("X-Internal-Token", "test-internal-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("HELLO"));
    }
}
