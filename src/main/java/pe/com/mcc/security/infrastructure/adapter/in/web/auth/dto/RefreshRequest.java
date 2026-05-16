package pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record RefreshRequest(@NotBlank String refreshToken) {}
