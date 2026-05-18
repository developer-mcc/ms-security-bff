package pe.com.mcc.security.application.auth.port.in;

import java.util.UUID;

public record VerificarResetOtpCommand(UUID preResetToken, String codigo, String direccionIp) {}
