package com.musiclog.shared.web;

import io.swagger.v3.oas.annotations.media.Schema;

/** Minimal public indication that the application can serve requests. */
@Schema(
        name = "HealthResponse",
        description = "Minimal application availability response.",
        additionalProperties = Schema.AdditionalPropertiesValue.FALSE,
        requiredProperties = "status")
public record HealthResponse(
        @Schema(
                        description = "Fixed basic availability indicator.",
                        allowableValues = "UP",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                String status) {}
