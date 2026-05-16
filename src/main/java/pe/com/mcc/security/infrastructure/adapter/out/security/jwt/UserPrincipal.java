package pe.com.mcc.security.infrastructure.adapter.out.security.jwt;

import java.util.List;
import java.util.UUID;
import pe.com.mcc.security.domain.permission.model.PermissionMap;

/**
 * Principal enriquecido. Lo construye el JwtAuthenticationFilter a partir de los claims del JWT.
 * Disponible en cualquier controller vía: (UserPrincipal)
 * SecurityContextHolder.getContext().getAuthentication().getPrincipal()
 */
public record UserPrincipal(
    UUID usuarioId,
    String nombreUsuario,
    UUID empresaId,
    UUID sucursalActiva,
    UUID sesionId,
    List<String> roles,
    PermissionMap permisos,
    List<UUID> sucursalesHabilitadas) {}
