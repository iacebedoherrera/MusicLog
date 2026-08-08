package com.musiclog.review;

import static org.assertj.core.api.Assertions.assertThat;

import com.musiclog.SpringModulithIntegrationTest;
import com.musiclog.review.dto.CreateReviewRequest;
import com.musiclog.review.dto.ReviewResponse;
import com.musiclog.review.events.ReviewCreatedEvent;
import com.musiclog.social.ActivityRepository;
import com.musiclog.user.User;
import com.musiclog.user.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

@SpringModulithIntegrationTest
@RecordApplicationEvents
class ReviewModuleIntegrationTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ApplicationEvents events;

    @Autowired
    private UserRepository userRepository;

    @Test
    void creatingReviewPublishesEventAndSocialConsumesIt() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User user = userRepository.save(new User("reviewer-" + suffix, suffix + "@example.test", "hash", "Reviewer"));
        UUID userId = user.getId();

        ReviewResponse response = reviewService.createReview(userId, new CreateReviewRequest(
                "album-mbid-" + UUID.randomUUID(),
                ReviewTargetType.ALBUM,
                9,
                "Great album",
                false));

        assertThat(events.stream(ReviewCreatedEvent.class))
                .anySatisfy(event -> assertThat(event.reviewId()).isEqualTo(response.id()));
        assertThat(activityRepository.findAll()).anySatisfy(activity -> assertThat(activity.getTargetId()).isEqualTo(response.id()));
        assertThat(response.author().username()).isEqualTo(user.getUsername());
    }
}
