package pe.com.mcc.security.domain.otp.event;

import java.time.Instant;
import java.util.UUID;
import pe.com.mcc.security.domain.otp.model.PropositoOtp;
import pe.com.mcc.security.domain.user.model.CanalOtp;

public record OtpSolicitadoEvent(
    UUID usuarioId,
    String codigoPlano,
    CanalOtp canal,
    PropositoOtp proposito,
    String direccionIp,
    Instant ocurridoEn) {}
