package com.musiclog.review;

import com.musiclog.review.dto.CreateReviewRequest;
import com.musiclog.review.dto.ListeningLogResponse;
import com.musiclog.review.dto.LogTrackRequest;
import com.musiclog.review.dto.ReviewResponse;
import com.musiclog.review.dto.UpdateReviewRequest;
import com.musiclog.review.events.ReviewCreatedEvent;
import com.musiclog.review.events.ReviewDeletedEvent;
import com.musiclog.review.events.ReviewUpdatedEvent;
import com.musiclog.review.events.TrackLoggedEvent;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
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
    private final ApplicationEventPublisher eventPublisher;

    public ReviewService(
            ReviewRepository reviewRepository,
            ListeningLogRepository listeningLogRepository,
            ApplicationEventPublisher eventPublisher) {
        this.reviewRepository = reviewRepository;
        this.listeningLogRepository = listeningLogRepository;
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
        return ReviewResponse.from(saved);
    }

    @Transactional
    public ReviewResponse updateReview(UUID userId, UUID reviewId, UpdateReviewRequest request) {
        validateReviewContent(request.rating(), request.reviewText());
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));
        review.update(request.rating(), normalize(request.reviewText()), request.containsSpoilers());
        eventPublisher.publishEvent(new ReviewUpdatedEvent(review.getId(), review.getUserId()));
        return ReviewResponse.from(review);
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
                .map(ReviewResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> myReviews(UUID userId, Pageable pageable) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(ReviewResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> reviewsForTarget(String mbid, ReviewTargetType targetType, Pageable pageable) {
        return reviewRepository.findByTargetMbidAndTargetTypeOrderByCreatedAtDesc(mbid, targetType, pageable)
                .map(ReviewResponse::from);
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
}
