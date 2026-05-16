package pe.com.mcc.security.domain.auth.event;

import java.time.Instant;

public record LoginFailedEvent(
    String nombreUsuarioIntento, String motivo, String direccionIp, Instant ocurridoEn) {}
