package com.musiclog.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.musiclog.shared.security.JwtService;
import com.musiclog.user.dto.RegisterRequest;
import com.musiclog.user.dto.UserProfileResponse;
import com.musiclog.user.events.UserRegisteredEvent;
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
        when(passwordEncoder.encode("password123")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResponse response = userService.register(new RegisterRequest("alice", "alice@example.com", "password123", "Alice"));

        assertThat(response.username()).isEqualTo("alice");
        verify(passwordEncoder).encode("password123");
        verify(eventPublisher).publishEvent(any(UserRegisteredEvent.class));
    }
}
