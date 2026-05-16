package pe.com.mcc.security.domain.token.event;

import java.time.Instant;
import java.util.UUID;
import pe.com.mcc.security.domain.token.model.MotivoRevocacion;

public record TokenRevokedEvent(
    UUID sesionId,
    UUID usuarioId,
    MotivoRevocacion motivo,
    int tokensRevocados,
    Instant ocurridoEn) {}
