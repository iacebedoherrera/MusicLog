package com.musiclog.social;

import com.musiclog.shared.security.AuthenticatedUser;
import com.musiclog.social.dto.ActivityResponse;
import com.musiclog.social.dto.LikeResponse;
import com.musiclog.shared.web.PageResponse;
import com.musiclog.shared.web.Pagination;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Social", description = "Feed de actividad, actividad pública y likes de reseñas.")
public class SocialController {

    private final FeedService feedService;

    public SocialController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping("/api/feed")
    @Operation(summary = "Obtener mi feed", description = "Devuelve actividad reciente de los usuarios seguidos.")
    @SecurityRequirement(name = "bearerAuth")
    public PageResponse<ActivityResponse> feed(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return feedService.feed(AuthenticatedUser.from(authentication).id(), page, size);
    }

    @GetMapping("/api/users/{username}/activity")
    @Operation(summary = "Obtener la actividad pública de un usuario", description = "Respuesta paginada uniforme; admite page y size.")
    public PageResponse<ActivityResponse> publicActivity(
            @PathVariable String username,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size) {
        return feedService.publicActivity(username, Pagination.pageable(page, size));
    }

    @PostMapping("/api/reviews/{id}/like")
    @Operation(summary = "Dar like a una reseña")
    @SecurityRequirement(name = "bearerAuth")
    public LikeResponse like(Authentication authentication, @PathVariable UUID id) {
        return feedService.likeReview(AuthenticatedUser.from(authentication).id(), id);
    }

    @DeleteMapping("/api/reviews/{id}/like")
    @Operation(summary = "Quitar mi like de una reseña")
    @SecurityRequirement(name = "bearerAuth")
    public LikeResponse unlike(Authentication authentication, @PathVariable UUID id) {
        return feedService.unlikeReview(AuthenticatedUser.from(authentication).id(), id);
    }

    @GetMapping("/api/reviews/{id}/likes")
    @Operation(summary = "Consultar los likes de una reseña")
    @SecurityRequirement(name = "bearerAuth")
    public LikeResponse likes(Authentication authentication, @PathVariable UUID id) {
        return feedService.likes(id, AuthenticatedUser.from(authentication).id());
    }
}
