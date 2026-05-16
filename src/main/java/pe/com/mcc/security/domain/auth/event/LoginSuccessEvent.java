package pe.com.mcc.security.domain.auth.event;

import java.time.Instant;
import java.util.UUID;

public record LoginSuccessEvent(
    UUID usuarioId,
    String nombreUsuario,
    UUID empresaId,
    UUID sucursalId,
    UUID sesionId,
    String direccionIp,
    Instant ocurridoEn) {}
