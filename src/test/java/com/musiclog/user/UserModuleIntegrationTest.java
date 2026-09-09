package com.musiclog.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.musiclog.SpringModulithIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musiclog.shared.security.JwtService;
import com.musiclog.user.dto.LoginRequest;
import com.musiclog.user.dto.RegisterRequest;
import com.musiclog.user.dto.UpdateAccountRequest;
import com.musiclog.user.dto.UserProfileResponse;
import com.musiclog.user.events.UserRegisteredEvent;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.http.MediaType;

@SpringModulithIntegrationTest
@AutoConfigureMockMvc
@RecordApplicationEvents
class UserModuleIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ApplicationEvents events;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerPublishesUserRegisteredEvent() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        UserProfileResponse profile = userService.register(new RegisterRequest(
                "user" + suffix,
                "user" + suffix + "@example.com",
                "password123",
                "User " + suffix));

        assertThat(events.stream(UserRegisteredEvent.class))
                .anySatisfy(event -> {
                    assertThat(event.userId()).isEqualTo(profile.id());
                    assertThat(event.username()).isEqualTo(profile.username());
                });
    }

    @Test
    void accountUpdateIsAuthenticatedAndAllowsTheNewPassword() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "account" + suffix;
        UserProfileResponse profile = userService.register(new RegisterRequest(
                username,
                username + "@example.com",
                "o".repeat(10),
                "Original " + suffix));
        String newPassword = "n".repeat(12);
        String updatedUsername = "updated" + suffix;
        String token = jwtService.createToken(profile.id(), profile.username());

        mockMvc.perform(put("/api/users/me/account")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateAccountRequest(
                                "Updated " + suffix,
                                updatedUsername,
                                newPassword,
                                newPassword))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(updatedUsername))
                .andExpect(jsonPath("$.displayName").value("Updated " + suffix))
                .andExpect(jsonPath("$.newPassword").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        assertThat(userService.login(new LoginRequest(updatedUsername, newPassword)).username())
                .isEqualTo(updatedUsername);
    }

    @Test
    void accountUpdateRejectsConflictsAndKeepsAllPreviousValues() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String firstUsername = "first" + suffix;
        String secondUsername = "second" + suffix;
        UserProfileResponse first = userService.register(new RegisterRequest(
                firstUsername,
                firstUsername + "@example.com",
                "a".repeat(10),
                "First " + suffix));
        userService.register(new RegisterRequest(
                secondUsername,
                secondUsername + "@example.com",
                "b".repeat(10),
                "Second " + suffix));
        String newPassword = "c".repeat(12);
        String token = jwtService.createToken(first.id(), first.username());
        String previousPasswordHash = userRepository.findById(first.id()).orElseThrow().getPasswordHash();

        String response = mockMvc.perform(put("/api/users/me/account")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateAccountRequest(
                                "Changed " + suffix,
                                secondUsername.toUpperCase(),
                                newPassword,
                                newPassword))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.fieldErrors.username").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).doesNotContain(newPassword);
        User unchangedUser = userRepository.findById(first.id()).orElseThrow();
        UserProfileResponse unchanged = userService.getProfile(first.id());
        assertThat(unchanged.username()).isEqualTo(firstUsername);
        assertThat(unchanged.displayName()).isEqualTo("First " + suffix);
        assertThat(unchangedUser.getPasswordHash()).isEqualTo(previousPasswordHash);
    }

    @Test
    void accountUpdateRejectsAnInvalidPairWithoutPartialChanges() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "invalid" + suffix;
        UserProfileResponse profile = userService.register(new RegisterRequest(
                username,
                username + "@example.com",
                "o".repeat(10),
                "Original " + suffix));
        String token = jwtService.createToken(profile.id(), profile.username());
        String previousPasswordHash = userRepository.findById(profile.id()).orElseThrow().getPasswordHash();

        mockMvc.perform(put("/api/users/me/account")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateAccountRequest(
                                "Changed " + suffix,
                                "changed" + suffix,
                                "x".repeat(8),
                                "y".repeat(8)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.newPasswordConfirmation").exists());

        User unchangedUser = userRepository.findById(profile.id()).orElseThrow();
        UserProfileResponse unchanged = userService.getProfile(profile.id());
        assertThat(unchanged.username()).isEqualTo(username);
        assertThat(unchanged.displayName()).isEqualTo("Original " + suffix);
        assertThat(unchangedUser.getPasswordHash()).isEqualTo(previousPasswordHash);
    }

    @Test
    void usernameIndexRejectsCaseOnlyDuplicatesWithoutChangingValidRows() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        // H2 no admite el índice funcional de PostgreSQL de V7; esta columna generada prueba la misma invariancia.
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN username_lower VARCHAR(50) AS (LOWER(username))");
        jdbcTemplate.execute("CREATE UNIQUE INDEX uk_users_username_lower ON users (username_lower)");
        User original = userRepository.saveAndFlush(new User(
                "CaseUser" + suffix,
                "case-first" + suffix + "@example.com",
                "hash-one",
                "First"));
        User other = userRepository.saveAndFlush(new User(
                "OtherUser" + suffix,
                "case-second" + suffix + "@example.com",
                "hash-two",
                "Second"));

        assertThatThrownBy(() -> userRepository.saveAndFlush(new User(
                "caseuser" + suffix,
                "case-duplicate" + suffix + "@example.com",
                "hash-three",
                "Duplicate")))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(userRepository.findById(original.getId()).orElseThrow().getUsername())
                .isEqualTo("CaseUser" + suffix);
        assertThat(userRepository.findById(other.getId()).orElseThrow().getUsername())
                .isEqualTo("OtherUser" + suffix);
    }

    @Test
    void accountUpdateRequiresAuthentication() throws Exception {
        mockMvc.perform(put("/api/users/me/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateAccountRequest("Ana", "ana", "", ""))))
                .andExpect(status().isForbidden());
    }
}
