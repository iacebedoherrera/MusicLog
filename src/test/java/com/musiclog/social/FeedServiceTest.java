package com.musiclog.social;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.musiclog.review.ReviewTargetType;
import com.musiclog.review.events.ReviewCreatedEvent;
import com.musiclog.user.UserService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

class FeedServiceTest {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void reviewCreatedEventUpdatesActorAndFollowerFeeds() {
        ActivityRepository activityRepository = mock(ActivityRepository.class);
        ReviewLikeRepository reviewLikeRepository = mock(ReviewLikeRepository.class);
        UserService userService = mock(UserService.class);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ListOperations listOperations = mock(ListOperations.class);
        HashOperations hashOperations = mock(HashOperations.class);
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(activityRepository.save(any(ActivityEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UUID actorId = UUID.randomUUID();
        UUID followerId = UUID.randomUUID();
        when(userService.followersOf(actorId)).thenReturn(List.of(followerId));
        FeedService feedService = new FeedService(activityRepository, reviewLikeRepository, userService, redisTemplate);
        ActivityEventListener listener = new ActivityEventListener(feedService);

        listener.onReviewCreated(new ReviewCreatedEvent(UUID.randomUUID(), actorId, "album-1", ReviewTargetType.ALBUM, 8));

        verify(listOperations).leftPush(eq("feed:" + actorId), anyString());
        verify(listOperations).leftPush(eq("feed:" + followerId), anyString());
    }
}
