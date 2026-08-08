package com.musiclog.shared.web;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.musiclog.SpringModulithIntegrationTest;
import com.musiclog.shared.security.JwtService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;

@SpringModulithIntegrationTest
@AutoConfigureMockMvc
@TestExecutionListeners(
        listeners = {ServletTestExecutionListener.class, DependencyInjectionTestExecutionListener.class},
        mergeMode = TestExecutionListeners.MergeMode.REPLACE_DEFAULTS)
class HealthControllerIntegrationTest {

    private static final String HEALTH_BODY = "{\"status\":\"UP\"}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void authenticatedGetReturnsTheExactHealthContract() throws Exception {
        mockMvc.perform(get("/health").header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(HEALTH_BODY));
    }

    @Test
    void unauthenticatedGetIsNotSuccessful() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void authenticatedPostIsMethodNotAllowedAndNeverReturnsTheHealthBody() throws Exception {
        mockMvc.perform(post("/health").header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().string(not(equalTo(HEALTH_BODY))));
    }

    @Test
    void generatedOpenApiDocumentsTheProtectedClosedHealthResponse() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/health'].get.security[0].bearerAuth").isArray())
                .andExpect(jsonPath("$.paths['/health'].get.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/HealthResponse"))
                .andExpect(jsonPath("$.paths['/health'].get.responses['405'].content").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.HealthResponse.additionalProperties").value(false))
                .andExpect(jsonPath("$.components.schemas.HealthResponse.required[0]").value("status"))
                .andExpect(jsonPath("$.components.schemas.HealthResponse.properties.status.enum[0]").value("UP"));
    }

    private String bearerToken() {
        return "Bearer " + jwtService.createToken(UUID.randomUUID(), "health-test-user");
    }
}
