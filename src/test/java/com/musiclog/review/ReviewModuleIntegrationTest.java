package com.musiclog.review;

import static org.assertj.core.api.Assertions.assertThat;

import com.musiclog.SpringModulithIntegrationTest;
import com.musiclog.review.dto.CreateReviewRequest;
import com.musiclog.review.dto.ReviewResponse;
import com.musiclog.review.events.ReviewCreatedEvent;
import com.musiclog.social.ActivityRepository;
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

    @Test
    void creatingReviewPublishesEventAndSocialConsumesIt() {
        UUID userId = UUID.randomUUID();

        ReviewResponse response = reviewService.createReview(userId, new CreateReviewRequest(
                "album-mbid-" + UUID.randomUUID(),
                ReviewTargetType.ALBUM,
                9,
                "Great album",
                false));

        assertThat(events.stream(ReviewCreatedEvent.class))
                .anySatisfy(event -> assertThat(event.reviewId()).isEqualTo(response.id()));
        assertThat(activityRepository.findAll()).anySatisfy(activity -> assertThat(activity.getTargetId()).isEqualTo(response.id()));
    }
}
