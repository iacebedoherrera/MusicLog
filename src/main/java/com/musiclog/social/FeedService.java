package com.musiclog.social;

import com.musiclog.review.events.ReviewCreatedEvent;
import com.musiclog.review.events.TrackLoggedEvent;
import com.musiclog.social.dto.ActivityResponse;
import com.musiclog.social.dto.FeedResponse;
import com.musiclog.social.dto.LikeResponse;
import com.musiclog.user.UserService;
import com.musiclog.user.events.UserFollowedEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedService {

    private static final Logger log = LoggerFactory.getLogger(FeedService.class);
    private static final int MAX_FEED_ITEMS = 200;

    private final ActivityRepository activityRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final UserService userService;
    private final StringRedisTemplate redisTemplate;

    public FeedService(
            ActivityRepository activityRepository,
            ReviewLikeRepository reviewLikeRepository,
            UserService userService,
            StringRedisTemplate redisTemplate) {
        this.activityRepository = activityRepository;
        this.reviewLikeRepository = reviewLikeRepository;
        this.userService = userService;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public ActivityEntry handleReviewCreated(ReviewCreatedEvent event) {
        ActivityEntry activity = activityRepository.save(new ActivityEntry(
                event.userId(),
                ActivityType.REVIEW_CREATED,
                event.reviewId(),
                event.targetMbid(),
                event.targetType().name()));
        fanoutToActorAndFollowers(activity);
        return activity;
    }

    @Transactional
    public ActivityEntry handleTrackLogged(TrackLoggedEvent event) {
        ActivityEntry activity = activityRepository.save(new ActivityEntry(
                event.userId(),
                ActivityType.TRACK_LOGGED,
                null,
                event.trackMbid(),
                "TRACK"));
        fanoutToActorAndFollowers(activity);
        return activity;
    }

    @Transactional
    public ActivityEntry handleUserFollowed(UserFollowedEvent event) {
        ActivityEntry activity = activityRepository.save(new ActivityEntry(
                event.followerId(),
                ActivityType.USER_FOLLOWED,
                event.followedId(),
                null,
                "USER"));
        pushToFeed(event.followedId(), activity);
        injectRecentActivities(event.followerId(), event.followedId());
        return activity;
    }

    public FeedResponse feed(UUID userId, int page, int size) {
        int normalizedSize = Math.max(1, Math.min(size, 50));
        int normalizedPage = Math.max(page, 0);
        long start = (long) normalizedPage * normalizedSize;
        long end = start + normalizedSize - 1;
        List<ActivityResponse> activities = new ArrayList<>();
        try {
            List<String> ids = redisTemplate.opsForList().range(feedKey(userId), start, end);
            if (ids == null) {
                return new FeedResponse(List.of(), normalizedPage, normalizedSize);
            }
            for (String id : ids) {
                readActivity(id).ifPresent(activities::add);
            }
        } catch (RedisConnectionFailureException exception) {
            log.debug("Feed Redis read skipped for user {}", userId, exception);
        }
        return new FeedResponse(activities, normalizedPage, normalizedSize);
    }

    @Transactional(readOnly = true)
    public Page<ActivityResponse> publicActivity(String username, Pageable pageable) {
        UUID actorId = userService.getUserIdByUsername(username);
        return activityRepository.findByActorIdOrderByCreatedAtDesc(actorId, pageable).map(ActivityResponse::from);
    }

    @Transactional
    public LikeResponse likeReview(UUID userId, UUID reviewId) {
        if (!reviewLikeRepository.existsByUserIdAndReviewId(userId, reviewId)) {
            try {
                reviewLikeRepository.save(new ReviewLike(userId, reviewId));
            } catch (DataIntegrityViolationException ignored) {
                log.debug("Duplicate review like ignored for user {} review {}", userId, reviewId);
            }
        }
        return likes(reviewId, userId);
    }

    @Transactional
    public LikeResponse unlikeReview(UUID userId, UUID reviewId) {
        reviewLikeRepository.deleteByUserIdAndReviewId(userId, reviewId);
        return likes(reviewId, userId);
    }

    @Transactional(readOnly = true)
    public LikeResponse likes(UUID reviewId, UUID currentUserId) {
        return new LikeResponse(
                reviewId,
                reviewLikeRepository.countByReviewId(reviewId),
                reviewLikeRepository.existsByUserIdAndReviewId(currentUserId, reviewId));
    }

    private void fanoutToActorAndFollowers(ActivityEntry activity) {
        Set<UUID> recipients = new LinkedHashSet<>();
        recipients.add(activity.getActorId());
        try {
            recipients.addAll(userService.followersOf(activity.getActorId()));
        } catch (RuntimeException exception) {
            log.debug("Could not load followers for activity fanout actor {}", activity.getActorId(), exception);
        }
        recipients.forEach(userId -> pushToFeed(userId, activity));
    }

    private void injectRecentActivities(UUID followerId, UUID followedId) {
        List<ActivityEntry> activities = new ArrayList<>(activityRepository.findTop20ByActorIdOrderByCreatedAtDesc(followedId));
        Collections.reverse(activities);
        activities.forEach(activity -> pushToFeed(followerId, activity));
    }

    private void pushToFeed(UUID userId, ActivityEntry activity) {
        try {
            writeActivityHash(activity);
            redisTemplate.opsForList().leftPush(feedKey(userId), activity.getId().toString());
            redisTemplate.opsForList().trim(feedKey(userId), 0, MAX_FEED_ITEMS - 1);
        } catch (RedisConnectionFailureException exception) {
            log.debug("Feed Redis write skipped for user {} activity {}", userId, activity.getId(), exception);
        }
    }

    private void writeActivityHash(ActivityEntry activity) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("id", activity.getId().toString());
        values.put("actorId", activity.getActorId().toString());
        values.put("activityType", activity.getActivityType().name());
        if (activity.getTargetId() != null) {
            values.put("targetId", activity.getTargetId().toString());
        }
        if (activity.getTargetMbid() != null) {
            values.put("targetMbid", activity.getTargetMbid());
        }
        if (activity.getTargetType() != null) {
            values.put("targetType", activity.getTargetType());
        }
        values.put("createdAt", activity.getCreatedAt().toString());
        redisTemplate.opsForHash().putAll(activityKey(activity.getId()), values);
    }

    private Optional<ActivityResponse> readActivity(String id) {
        try {
            Map<Object, Object> values = redisTemplate.opsForHash().entries("activity:" + id);
            if (values.isEmpty()) {
                return activityRepository.findById(UUID.fromString(id)).map(ActivityResponse::from);
            }
            return Optional.of(new ActivityResponse(
                    UUID.fromString((String) values.get("id")),
                    UUID.fromString((String) values.get("actorId")),
                    ActivityType.valueOf((String) values.get("activityType")),
                    uuidOrNull((String) values.get("targetId")),
                    (String) values.get("targetMbid"),
                    (String) values.get("targetType"),
                    Instant.parse((String) values.get("createdAt"))));
        } catch (RuntimeException exception) {
            log.debug("Could not read activity {} from Redis", id, exception);
            return Optional.empty();
        }
    }

    private UUID uuidOrNull(String value) {
        return value == null ? null : UUID.fromString(value);
    }

    private String feedKey(UUID userId) {
        return "feed:" + userId;
    }

    private String activityKey(UUID activityId) {
        return "activity:" + activityId;
    }
}
