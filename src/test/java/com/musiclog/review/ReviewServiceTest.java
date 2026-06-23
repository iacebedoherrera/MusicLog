package com.musiclog.review;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.musiclog.review.dto.CreateReviewRequest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class ReviewServiceTest {

    private final ReviewRepository reviewRepository = mock(ReviewRepository.class);
    private final ListeningLogRepository listeningLogRepository = mock(ListeningLogRepository.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final ReviewService reviewService = new ReviewService(reviewRepository, listeningLogRepository, eventPublisher);

    @Test
    void createReviewRequiresRatingOrText() {
        CreateReviewRequest request = new CreateReviewRequest("album-1", ReviewTargetType.ALBUM, null, " ", false);

        assertThatThrownBy(() -> reviewService.createReview(UUID.randomUUID(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least rating or review text");
    }

    @Test
    void createReviewRejectsRatingOutsideRange() {
        CreateReviewRequest request = new CreateReviewRequest("album-1", ReviewTargetType.ALBUM, 11, null, false);

        assertThatThrownBy(() -> reviewService.createReview(UUID.randomUUID(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 1 and 10");
    }
}
