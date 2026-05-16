package pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record TokenResponse(
    UUID sesionId,
    String accessToken,
    LocalDateTime accessExpiraEn,
    String refreshToken,
    LocalDateTime refreshExpiraEn,
    String tokenType) {}
