package pe.com.mcc.security.application.auth.port.in;

import java.util.UUID;
import pe.com.mcc.security.domain.auth.model.DispositivoInfo;

/**
 * Datos necesarios para cambiar la sucursal activa del usuario actual. - usuarioId / sesionIdActual
 * / sucursalActual: tomados del UserPrincipal del request. - sucursalNueva: parámetro del path
 * /auth/switch-branch/{sucursalId}. - dispositivo: contexto técnico para auditoría.
 */
public record SwitchBranchCommand(
    UUID usuarioId,
    UUID sesionIdActual,
    UUID sucursalActual,
    UUID sucursalNueva,
    DispositivoInfo dispositivo) {}
