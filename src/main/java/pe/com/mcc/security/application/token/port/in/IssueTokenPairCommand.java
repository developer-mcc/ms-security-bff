package pe.com.mcc.security.application.token.port.in;

import java.util.List;
import java.util.UUID;
import pe.com.mcc.security.domain.auth.model.DispositivoInfo;
import pe.com.mcc.security.domain.permission.model.PermissionMap;
import pe.com.mcc.security.domain.user.model.Usuario;

/**
 * Datos necesarios para emitir un par access+refresh con la misma sesionId. sesionId puede venir
 * nula (primer login) o reusarse en refresh-rotation.
 */
public record IssueTokenPairCommand(
    Usuario usuario,
    UUID empresaId,
    UUID sucursalActiva,
    List<UUID> sucursalesHabilitadas,
    PermissionMap permisos,
    DispositivoInfo dispositivo,
    UUID sesionIdReuso) {}
