package pe.com.mcc.security.application.otp.port.in;

import java.util.UUID;
import pe.com.mcc.security.domain.otp.model.PropositoOtp;

public record VerificarOtpCommand(
    UUID usuarioId, String codigo, PropositoOtp proposito, String direccionIp) {}
