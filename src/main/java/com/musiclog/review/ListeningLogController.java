package com.musiclog.review;

import com.musiclog.review.dto.ListeningLogResponse;
import com.musiclog.review.dto.LogTrackRequest;
import com.musiclog.shared.security.AuthenticatedUser;
import com.musiclog.shared.web.PageResponse;
import com.musiclog.shared.web.Pagination;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Listening log", description = "Registro de escuchas manuales del usuario autenticado.")
public class ListeningLogController {

    private final ReviewService reviewService;

    public ListeningLogController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/api/listening-log")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar una escucha", description = "Si listenedAt se omite, se registra la escucha en el momento actual.")
    @SecurityRequirement(name = "bearerAuth")
    public ListeningLogResponse log(Authentication authentication, @Valid @RequestBody LogTrackRequest request) {
        return reviewService.logTrack(AuthenticatedUser.from(authentication).id(), request, ListeningSource.MANUAL);
    }

    @GetMapping("/api/listening-log/me")
    @Operation(summary = "Listar mi historial de escuchas", description = "Respuesta paginada uniforme; admite page y size.")
    @SecurityRequirement(name = "bearerAuth")
    public PageResponse<ListeningLogResponse> mine(
            Authentication authentication,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(reviewService.myListeningLog(AuthenticatedUser.from(authentication).id(), Pagination.pageable(page, size)));
    }
}
