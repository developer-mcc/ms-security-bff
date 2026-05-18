package pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto;

import java.util.UUID;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record VerificarResetOtpResponse(UUID resetToken) {}
