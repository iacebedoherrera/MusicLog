package com.musiclog.review;

import com.musiclog.review.dto.ListeningLogResponse;
import com.musiclog.review.dto.LogTrackRequest;
import com.musiclog.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ListeningLogController {

    private final ReviewService reviewService;

    public ListeningLogController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/api/listening-log")
    @ResponseStatus(HttpStatus.CREATED)
    public ListeningLogResponse log(Authentication authentication, @Valid @RequestBody LogTrackRequest request) {
        return reviewService.logTrack(AuthenticatedUser.from(authentication).id(), request, ListeningSource.MANUAL);
    }

    @GetMapping("/api/listening-log/me")
    public Page<ListeningLogResponse> mine(Authentication authentication, Pageable pageable) {
        return reviewService.myListeningLog(AuthenticatedUser.from(authentication).id(), pageable);
    }
}
