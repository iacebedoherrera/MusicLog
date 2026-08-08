package com.musiclog.shared.web;

import com.musiclog.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "System", description = "Basic application availability")
class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Get the basic application availability")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(
            responseCode = "200",
            description = "Application is available",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HealthResponse.class)))
    @ApiResponse(responseCode = "405", description = "Only GET is supported", content = @Content)
    HealthResponse health(Authentication authentication) {
        AuthenticatedUser.from(authentication);
        return new HealthResponse("UP");
    }
}
