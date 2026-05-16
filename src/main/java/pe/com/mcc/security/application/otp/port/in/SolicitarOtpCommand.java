package pe.com.mcc.security.application.otp.port.in;

import java.util.UUID;
import pe.com.mcc.security.domain.otp.model.PropositoOtp;
import pe.com.mcc.security.domain.user.model.CanalOtp;

public record SolicitarOtpCommand(
    UUID usuarioId, CanalOtp canal, PropositoOtp proposito, String direccionIp) {}
