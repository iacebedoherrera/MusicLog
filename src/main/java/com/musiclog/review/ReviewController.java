package com.musiclog.review;

import com.musiclog.review.dto.CreateReviewRequest;
import com.musiclog.review.dto.ReviewResponse;
import com.musiclog.review.dto.UpdateReviewRequest;
import com.musiclog.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/api/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(Authentication authentication, @Valid @RequestBody CreateReviewRequest request) {
        return reviewService.createReview(AuthenticatedUser.from(authentication).id(), request);
    }

    @PutMapping("/api/reviews/{id}")
    public ReviewResponse update(Authentication authentication, @PathVariable UUID id, @Valid @RequestBody UpdateReviewRequest request) {
        return reviewService.updateReview(AuthenticatedUser.from(authentication).id(), id, request);
    }

    @DeleteMapping("/api/reviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication, @PathVariable UUID id) {
        reviewService.deleteReview(AuthenticatedUser.from(authentication).id(), id);
    }

    @GetMapping("/api/reviews/{id}")
    public ReviewResponse get(@PathVariable UUID id) {
        return reviewService.getReview(id);
    }

    @GetMapping("/api/reviews/me")
    public Page<ReviewResponse> mine(Authentication authentication, Pageable pageable) {
        return reviewService.myReviews(AuthenticatedUser.from(authentication).id(), pageable);
    }

    @GetMapping("/api/catalog/albums/{mbid}/reviews")
    public Page<ReviewResponse> albumReviews(@PathVariable String mbid, Pageable pageable) {
        return reviewService.reviewsForTarget(mbid, ReviewTargetType.ALBUM, pageable);
    }

    @GetMapping("/api/catalog/artists/{mbid}/reviews")
    public Page<ReviewResponse> artistReviews(@PathVariable String mbid, Pageable pageable) {
        return reviewService.reviewsForTarget(mbid, ReviewTargetType.ARTIST, pageable);
    }
}
