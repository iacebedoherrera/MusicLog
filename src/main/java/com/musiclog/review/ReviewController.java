package com.musiclog.review;

import com.musiclog.review.dto.CreateReviewRequest;
import com.musiclog.review.dto.ReviewResponse;
import com.musiclog.review.dto.UpdateReviewRequest;
import com.musiclog.shared.security.AuthenticatedUser;
import com.musiclog.shared.web.PageResponse;
import com.musiclog.shared.web.Pagination;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
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
@Tag(name = "Reseñas", description = "Valoraciones y reseñas de artistas, álbumes y pistas.")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/api/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una reseña", description = "La puntuación debe estar entre 1 y 10.")
    @SecurityRequirement(name = "bearerAuth")
    public ReviewResponse create(Authentication authentication, @Valid @RequestBody CreateReviewRequest request) {
        return reviewService.createReview(AuthenticatedUser.from(authentication).id(), request);
    }

    @PutMapping("/api/reviews/{id}")
    @Operation(summary = "Actualizar mi reseña")
    @SecurityRequirement(name = "bearerAuth")
    public ReviewResponse update(Authentication authentication, @PathVariable UUID id, @Valid @RequestBody UpdateReviewRequest request) {
        return reviewService.updateReview(AuthenticatedUser.from(authentication).id(), id, request);
    }

    @DeleteMapping("/api/reviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar mi reseña")
    @SecurityRequirement(name = "bearerAuth")
    public void delete(Authentication authentication, @PathVariable UUID id) {
        reviewService.deleteReview(AuthenticatedUser.from(authentication).id(), id);
    }

    @GetMapping("/api/reviews/{id}")
    @Operation(summary = "Obtener una reseña")
    public ReviewResponse get(@PathVariable UUID id) {
        return reviewService.getReview(id);
    }

    @GetMapping("/api/reviews/me")
    @Operation(summary = "Listar mis reseñas", description = "Respuesta paginada uniforme; admite page y size.")
    @SecurityRequirement(name = "bearerAuth")
    public PageResponse<ReviewResponse> mine(
            Authentication authentication,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size) {
        return reviewService.myReviews(AuthenticatedUser.from(authentication).id(), Pagination.pageable(page, size));
    }

    @GetMapping("/api/catalog/albums/{mbid}/reviews")
    @Operation(summary = "Listar reseñas de un álbum", description = "Respuesta paginada uniforme; admite page y size.")
    public PageResponse<ReviewResponse> albumReviews(
            @PathVariable String mbid,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size) {
        return reviewService.reviewsForTarget(mbid, ReviewTargetType.ALBUM, Pagination.pageable(page, size));
    }

    @GetMapping("/api/catalog/artists/{mbid}/reviews")
    @Operation(summary = "Listar reseñas de un artista", description = "Respuesta paginada uniforme; admite page y size.")
    public PageResponse<ReviewResponse> artistReviews(
            @PathVariable String mbid,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size) {
        return reviewService.reviewsForTarget(mbid, ReviewTargetType.ARTIST, Pagination.pageable(page, size));
    }
}
