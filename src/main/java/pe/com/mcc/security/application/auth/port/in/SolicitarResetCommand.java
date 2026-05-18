package pe.com.mcc.security.application.auth.port.in;

import pe.com.mcc.security.domain.user.model.CanalOtp;

public record SolicitarResetCommand(String contacto, CanalOtp canal, String direccionIp) {}
