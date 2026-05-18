package pe.com.mcc.security.application.auth.port.in;

import java.util.UUID;

public record VerificarResetOtpResult(UUID resetToken) {}
