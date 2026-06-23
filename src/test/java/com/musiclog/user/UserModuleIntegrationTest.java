package com.musiclog.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.musiclog.SpringModulithIntegrationTest;
import com.musiclog.user.dto.RegisterRequest;
import com.musiclog.user.dto.UserProfileResponse;
import com.musiclog.user.events.UserRegisteredEvent;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

@SpringModulithIntegrationTest
@RecordApplicationEvents
class UserModuleIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEvents events;

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
}
