package com.musiclog.review;

import com.musiclog.review.dto.CreateReviewRequest;
import com.musiclog.review.dto.ListeningLogResponse;
import com.musiclog.review.dto.LogTrackRequest;
import com.musiclog.review.dto.ReviewResponse;
import com.musiclog.review.dto.ReviewAuthorResponse;
import com.musiclog.review.dto.UpdateReviewRequest;
import com.musiclog.shared.web.PageResponse;
import com.musiclog.user.UserService;
import com.musiclog.user.dto.UserProfileResponse;
import com.musiclog.review.events.ReviewCreatedEvent;
import com.musiclog.review.events.ReviewDeletedEvent;
import com.musiclog.review.events.ReviewUpdatedEvent;
import com.musiclog.review.events.TrackLoggedEvent;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ListeningLogRepository listeningLogRepository;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    public ReviewService(
            ReviewRepository reviewRepository,
            ListeningLogRepository listeningLogRepository,
            UserService userService,
            ApplicationEventPublisher eventPublisher) {
        this.reviewRepository = reviewRepository;
        this.listeningLogRepository = listeningLogRepository;
        this.userService = userService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ReviewResponse createReview(UUID userId, CreateReviewRequest request) {
        validateReviewContent(request.rating(), request.reviewText());
        if (reviewRepository.existsByUserIdAndTargetMbid(userId, request.targetMbid())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already reviewed this target");
        }
        Review review = new Review(
                userId,
                request.targetMbid(),
                request.targetType(),
                request.rating(),
                normalize(request.reviewText()),
                request.containsSpoilers());
        Review saved = reviewRepository.save(review);
        eventPublisher.publishEvent(new ReviewCreatedEvent(
                saved.getId(),
                saved.getUserId(),
                saved.getTargetMbid(),
                saved.getTargetType(),
                saved.getRating()));
        return response(saved);
    }

    @Transactional
    public ReviewResponse updateReview(UUID userId, UUID reviewId, UpdateReviewRequest request) {
        validateReviewContent(request.rating(), request.reviewText());
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));
        review.update(request.rating(), normalize(request.reviewText()), request.containsSpoilers());
        eventPublisher.publishEvent(new ReviewUpdatedEvent(review.getId(), review.getUserId()));
        return response(review);
    }

    @Transactional
    public void deleteReview(UUID userId, UUID reviewId) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));
        reviewRepository.delete(review);
        eventPublisher.publishEvent(new ReviewDeletedEvent(review.getId(), userId, review.getTargetMbid(), review.getTargetType()));
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReview(UUID reviewId) {
        return reviewRepository.findById(reviewId)
                .map(this::response)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> myReviews(UUID userId, Pageable pageable) {
        return responses(reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable));
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> reviewsForTarget(String mbid, ReviewTargetType targetType, Pageable pageable) {
        return responses(reviewRepository.findByTargetMbidAndTargetTypeOrderByCreatedAtDesc(mbid, targetType, pageable));
    }

    @Transactional
    public ListeningLogResponse logTrack(UUID userId, LogTrackRequest request, ListeningSource source) {
        return logTrack(userId, request.trackMbid(), request.listenedAt(), source);
    }

    @Transactional
    public ListeningLogResponse logTrack(UUID userId, String trackMbid, Instant listenedAt, ListeningSource source) {
        Instant effectiveListenedAt = listenedAt == null ? Instant.now() : listenedAt;
        ListeningLog saved = listeningLogRepository.save(new ListeningLog(userId, trackMbid, effectiveListenedAt, source));
        eventPublisher.publishEvent(new TrackLoggedEvent(userId, saved.getTrackMbid(), saved.getListenedAt(), saved.getSource()));
        return ListeningLogResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<ListeningLogResponse> myListeningLog(UUID userId, Pageable pageable) {
        return listeningLogRepository.findByUserIdOrderByListenedAtDesc(userId, pageable).map(ListeningLogResponse::from);
    }

    private void validateReviewContent(Integer rating, String reviewText) {
        if (rating == null && (reviewText == null || reviewText.isBlank())) {
            throw new IllegalArgumentException("Review requires at least rating or review text");
        }
        if (rating != null && (rating < 1 || rating > 10)) {
            throw new IllegalArgumentException("Rating must be between 1 and 10");
        }
    }

    private String normalize(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }

    private ReviewResponse response(Review review) {
        return ReviewResponse.from(review, ReviewAuthorResponse.from(userService.getProfile(review.getUserId())));
    }

    private PageResponse<ReviewResponse> responses(Page<Review> reviews) {
        List<Review> items = reviews.getContent();
        Map<UUID, UserProfileResponse> authors = userService.profilesByIdsAsMap(items.stream()
                .map(Review::getUserId)
                .distinct()
                .toList());
        List<ReviewResponse> responses = items.stream()
                .map(review -> ReviewResponse.from(review, ReviewAuthorResponse.from(authors.get(review.getUserId()))))
                .toList();
        return PageResponse.from(reviews, responses);
    }
}
