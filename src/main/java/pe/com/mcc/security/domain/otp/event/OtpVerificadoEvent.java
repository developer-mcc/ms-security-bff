package pe.com.mcc.security.domain.otp.event;

import java.time.Instant;
import java.util.UUID;
import pe.com.mcc.security.domain.otp.model.PropositoOtp;

public record OtpVerificadoEvent(
    UUID usuarioId, PropositoOtp proposito, String direccionIp, Instant ocurridoEn) {}
