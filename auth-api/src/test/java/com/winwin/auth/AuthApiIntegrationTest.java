package com.winwin.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.winwin.auth.process.DataApiClient;
import com.winwin.auth.process.ProcessingLogRepository;
import com.winwin.auth.error.DataApiUnavailableException;
import com.winwin.auth.user.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:authdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "JWT_SECRET=dGVzdC1qd3Qtc2VjcmV0LW11c3QtYmUtYXQtbGVhc3QtMzItYnl0ZXMtbG9uZw==",
        "INTERNAL_TOKEN=test-internal-token",
        "DATA_API_URL=http://localhost:9999"
})
@AutoConfigureMockMvc
class AuthApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository users;

    @Autowired
    private ProcessingLogRepository logs;

    @MockitoBean
    private DataApiClient dataApiClient;

    @BeforeEach
    void cleanDatabase() {
        logs.deleteAll();
        users.deleteAll();
    }

    @Test
    void registersUserWithBcryptPassword() throws Exception {
        register("user@example.com", "password").andExpect(status().isCreated());

        var user = users.findByEmail("user@example.com").orElseThrow();
        assertThat(user.getPasswordHash()).startsWith("$2");
        assertThat(user.getPasswordHash()).doesNotContain("password");
    }

    @Test
    void rejectsDuplicateEmail() throws Exception {
        register("user@example.com", "password").andExpect(status().isCreated());
        register("USER@example.com", "different")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email is already registered"));
    }

    @Test
    void logsInWithValidCredentials() throws Exception {
        register("user@example.com", "password").andExpect(status().isCreated());

        login("user@example.com", "password")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void rejectsInvalidLoginCredentials() throws Exception {
        register("user@example.com", "password").andExpect(status().isCreated());

        login("user@example.com", "wrong-password")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }

    @Test
    void rejectsUnauthenticatedProcessingRequest() throws Exception {
        mockMvc.perform(post("/api/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hello\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void successfulProcessingCreatesLog() throws Exception {
        register("user@example.com", "password").andExpect(status().isCreated());
        String token = tokenFrom(login("user@example.com", "password")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        when(dataApiClient.transform("hello")).thenReturn("HELLO");

        mockMvc.perform(post("/api/process")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("HELLO"));

        assertThat(logs.findAll()).singleElement().satisfies(log -> {
            assertThat(log.getInputText()).isEqualTo("hello");
            assertThat(log.getOutputText()).isEqualTo("HELLO");
            assertThat(log.getUser().getId())
                    .isEqualTo(users.findByEmail("user@example.com").orElseThrow().getId());
            assertThat(log.getCreatedAt()).isNotNull();
        });
    }

    @Test
    void failedTransformationDoesNotCreateLog() throws Exception {
        register("user@example.com", "password").andExpect(status().isCreated());
        String token = tokenFrom(login("user@example.com", "password")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        when(dataApiClient.transform("hello")).thenThrow(new DataApiUnavailableException());

        mockMvc.perform(post("/api/process")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hello\"}"))
                .andExpect(status().isBadGateway());

        assertThat(logs.count()).isZero();
    }

    private org.springframework.test.web.servlet.ResultActions register(String email, String password)
            throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentials(email, password)));
    }

    private org.springframework.test.web.servlet.ResultActions login(String email, String password)
            throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentials(email, password)));
    }

    private String credentials(String email, String password) throws Exception {
        return objectMapper.writeValueAsString(java.util.Map.of("email", email, "password", password));
    }

    private String tokenFrom(String response) throws Exception {
        JsonNode body = objectMapper.readTree(response);
        return body.get("token").asText();
    }
}
