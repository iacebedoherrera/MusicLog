package com.musiclog.social;

import com.musiclog.shared.security.AuthenticatedUser;
import com.musiclog.social.dto.ActivityResponse;
import com.musiclog.social.dto.FeedResponse;
import com.musiclog.social.dto.LikeResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SocialController {

    private final FeedService feedService;

    public SocialController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping("/api/feed")
    public FeedResponse feed(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return feedService.feed(AuthenticatedUser.from(authentication).id(), page, size);
    }

    @GetMapping("/api/users/{username}/activity")
    public Page<ActivityResponse> publicActivity(@PathVariable String username, Pageable pageable) {
        return feedService.publicActivity(username, pageable);
    }

    @PostMapping("/api/reviews/{id}/like")
    public LikeResponse like(Authentication authentication, @PathVariable UUID id) {
        return feedService.likeReview(AuthenticatedUser.from(authentication).id(), id);
    }

    @DeleteMapping("/api/reviews/{id}/like")
    public LikeResponse unlike(Authentication authentication, @PathVariable UUID id) {
        return feedService.unlikeReview(AuthenticatedUser.from(authentication).id(), id);
    }

    @GetMapping("/api/reviews/{id}/likes")
    public LikeResponse likes(Authentication authentication, @PathVariable UUID id) {
        return feedService.likes(id, AuthenticatedUser.from(authentication).id());
    }
}
