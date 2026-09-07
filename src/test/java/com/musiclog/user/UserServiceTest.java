package com.musiclog.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.musiclog.shared.exception.ApiFieldErrorException;
import com.musiclog.shared.security.JwtService;
import com.musiclog.user.dto.RegisterRequest;
import com.musiclog.user.dto.UpdateAccountRequest;
import com.musiclog.user.dto.UserProfileResponse;
import com.musiclog.user.events.UserRegisteredEvent;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final FollowRepository followRepository = mock(FollowRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final UserService userService = new UserService(userRepository, followRepository, passwordEncoder, jwtService, eventPublisher);

    @Test
    void registerHashesPasswordAndPublishesEvent() {
        String password = "p".repeat(12);
        when(passwordEncoder.encode(password)).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResponse response = userService.register(new RegisterRequest("alice", "alice@example.com", password, "Alice"));

        assertThat(response.username()).isEqualTo("alice");
        verify(passwordEncoder).encode(password);
        verify(eventPublisher).publishEvent(any(UserRegisteredEvent.class));
    }

    @Test
    void updateAccountChangesIdentityAndHashesOnlyTheNewPassword() {
        User user = new User("old_name", "old@example.com", "old-hash", "Old name");
        String newPassword = "n".repeat(12);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameIgnoreCaseAndIdNot("new_name", user.getId())).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn("new-hash");

        UserProfileResponse response = userService.updateAccount(
                user.getId(),
                new UpdateAccountRequest("New name", "new_name", newPassword, newPassword));

        assertThat(response.displayName()).isEqualTo("New name");
        assertThat(response.username()).isEqualTo("new_name");
        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(user);
        verify(userRepository).flush();
    }

    @Test
    void updateAccountRejectsAnOccupiedUsernameBeforeMutating() {
        User user = new User("old_name", "old@example.com", "old-hash", "Old name");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameIgnoreCaseAndIdNot("TAKEN", user.getId())).thenReturn(true);

        assertThatThrownBy(() -> userService.updateAccount(
                user.getId(),
                new UpdateAccountRequest("New name", "TAKEN", "", "")))
                .isInstanceOf(ApiFieldErrorException.class)
                .satisfies(error -> {
                    ApiFieldErrorException fieldError = (ApiFieldErrorException) error;
                    assertThat(fieldError.status().value()).isEqualTo(409);
                    assertThat(fieldError.fieldErrors()).containsKey("username");
                });

        assertThat(user.getDisplayName()).isEqualTo("Old name");
        assertThat(user.getUsername()).isEqualTo("old_name");
        assertThat(user.getPasswordHash()).isEqualTo("old-hash");
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateAccountRejectsMismatchedPasswordsWithoutMutating() {
        User user = new User("old_name", "old@example.com", "old-hash", "Old name");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.updateAccount(
                user.getId(),
                new UpdateAccountRequest("New name", "new_name", "a".repeat(8), "b".repeat(8))))
                .isInstanceOf(ApiFieldErrorException.class)
                .satisfies(error -> assertThat(((ApiFieldErrorException) error).fieldErrors())
                        .containsKey("newPasswordConfirmation"));

        assertThat(user.getDisplayName()).isEqualTo("Old name");
        assertThat(user.getUsername()).isEqualTo("old_name");
        assertThat(user.getPasswordHash()).isEqualTo("old-hash");
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateAccountRejectsInvalidPasswordLengthWithoutMutating() {
        User user = new User("old_name", "old@example.com", "old-hash", "Old name");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.updateAccount(
                user.getId(),
                new UpdateAccountRequest("New name", "new_name", "short", "short")))
                .isInstanceOf(ApiFieldErrorException.class)
                .satisfies(error -> assertThat(((ApiFieldErrorException) error).fieldErrors())
                        .containsKey("newPassword"));

        assertThat(user.getPasswordHash()).isEqualTo("old-hash");
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateAccountKeepsThePasswordWhenTheOptionalPairIsEmpty() {
        User user = new User("old_name", "old@example.com", "old-hash", "Old name");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameIgnoreCaseAndIdNot("old_name", user.getId())).thenReturn(false);

        UserProfileResponse response = userService.updateAccount(
                user.getId(),
                new UpdateAccountRequest("New name", "old_name", "", ""));

        assertThat(response.displayName()).isEqualTo("New name");
        assertThat(user.getPasswordHash()).isEqualTo("old-hash");
        verify(passwordEncoder, never()).encode(any());
    }
}
