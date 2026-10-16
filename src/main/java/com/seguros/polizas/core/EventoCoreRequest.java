package com.seguros.polizas.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventoCoreRequest(
        @NotBlank String evento,
        @NotNull Long polizaId
) {
}
