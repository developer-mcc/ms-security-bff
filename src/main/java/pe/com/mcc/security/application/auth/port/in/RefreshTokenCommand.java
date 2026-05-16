package pe.com.mcc.security.application.auth.port.in;

import pe.com.mcc.security.domain.auth.model.DispositivoInfo;

public record RefreshTokenCommand(String refreshJwt, DispositivoInfo dispositivo) {}
