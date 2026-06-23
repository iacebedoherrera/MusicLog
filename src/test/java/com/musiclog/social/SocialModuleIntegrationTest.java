package com.musiclog.social;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.musiclog.SpringModulithIntegrationTest;
import com.musiclog.review.ReviewTargetType;
import com.musiclog.review.events.ReviewCreatedEvent;
import com.musiclog.user.UserService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringModulithIntegrationTest
class SocialModuleIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ActivityRepository activityRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @MockBean
    private ListOperations<String, String> listOperations;

    @MockBean
    private HashOperations<String, Object, Object> hashOperations;

    @BeforeEach
    void setUp() {
        activityRepository.deleteAll();
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    void reviewCreatedEventCreatesActivityAndUpdatesRedisFeed() {
        UUID actorId = UUID.randomUUID();
        UUID followerId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        when(userService.followersOf(actorId)).thenReturn(List.of(followerId));

        eventPublisher.publishEvent(new ReviewCreatedEvent(reviewId, actorId, "album-1", ReviewTargetType.ALBUM, 8));

        assertThat(activityRepository.findAll()).anySatisfy(activity -> assertThat(activity.getTargetId()).isEqualTo(reviewId));
        verify(listOperations).leftPush(eq("feed:" + actorId), anyString());
        verify(listOperations).leftPush(eq("feed:" + followerId), anyString());
    }
}
