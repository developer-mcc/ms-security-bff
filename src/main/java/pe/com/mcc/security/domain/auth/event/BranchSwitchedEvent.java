package pe.com.mcc.security.domain.auth.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Se publica cuando un usuario cambia de sucursal activa. sesionAnterior queda revocada (motivo
 * BRANCH_SWITCH); sesionNueva trae los tokens recién emitidos.
 */
public record BranchSwitchedEvent(
    UUID usuarioId,
    String nombreUsuario,
    UUID empresaId,
    UUID sesionAnterior,
    UUID sesionNueva,
    UUID sucursalAnterior,
    UUID sucursalNueva,
    String direccionIp,
    Instant ocurridoEn) {}
