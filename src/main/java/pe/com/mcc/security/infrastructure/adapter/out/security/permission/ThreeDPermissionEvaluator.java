package pe.com.mcc.security.infrastructure.adapter.out.security.permission;

import java.io.Serializable;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.domain.permission.model.Accion;
import pe.com.mcc.security.domain.permission.model.Alcance;
import pe.com.mcc.security.domain.permission.model.PermissionMap;
import pe.com.mcc.security.infrastructure.adapter.out.security.jwt.UserPrincipal;

/**
 * PermissionEvaluator del modelo 3D (recurso x acción x alcance).
 *
 * <p>Habilita expresiones SpEL en @PreAuthorize: @PreAuthorize("hasPermission(null, 'USER',
 * 'CREATE')") → ¿el rol tiene permiso CREATE sobre USER en cualquier alcance
 * permitido? @PreAuthorize("hasPermission(#sucursalId, 'BRANCH', 'UPDATE')") → ¿puede el usuario
 * UPDATE BRANCH? Si tiene OWN_BRANCH, además #sucursalId debe coincidir con la sucursal activa del
 * UserPrincipal.
 *
 * <p>Si el target (sucursalId) es null, el evaluador asume alcance OWN_BRANCH como mínimo — es la
 * política más restrictiva. ALL_BRANCHES también satisface ese requerimiento.
 */
@NullMarked
@Slf4j
@Component
public class ThreeDPermissionEvaluator implements PermissionEvaluator {

  /**
   * Firma usada por: hasPermission(targetDomainObject, permission) En este modelo NO se usa porque
   * siempre necesitamos saber el recurso. La dejamos cerrada para forzar el uso de la firma de 4
   * parámetros.
   */
  @Override
  public boolean hasPermission(
      @Nullable Authentication authentication,
      @Nullable Object targetDomainObject,
      Object permission) {
    log.debug(
        "hasPermission(target, permission) no soportado — use hasPermission(id, type, action)");
    return false;
  }

  /**
   * Firma principal: hasPermission(#sucursalId, 'BRANCH', 'UPDATE')
   *
   * @param targetId UUID de la sucursal afectada (puede ser null si la acción no es scope-aware)
   * @param targetType recurso del modelo 3D ('USER', 'BRANCH', 'PRODUCT', ...)
   * @param permission acción del modelo 3D ('READ', 'CREATE', 'UPDATE', 'DELETE')
   */
  @Override
  public boolean hasPermission(
      @Nullable Authentication authentication,
      @Nullable Serializable targetId,
      String targetType,
      Object permission) {

    UserPrincipal principal = principalDe(authentication);
    if (principal == null) {
      return false;
    }

    Accion accion = parseAccion(permission);
    if (accion == null) {
      return false;
    }

    Alcance alcanceRequerido = resolverAlcanceRequerido(targetId, principal);
    PermissionMap mapa = principal.permisos();
    boolean concedido = mapa.concede(targetType, accion, alcanceRequerido);

    log.debug(
        "hasPermission usuario={} recurso={} accion={} scope={} -> {}",
        principal.nombreUsuario(),
        targetType,
        accion,
        alcanceRequerido,
        concedido);
    return concedido;
  }

  /**
   * Política: - Si el caller no especifica targetId → OWN_BRANCH (mínimo). ALL_BRANCHES también
   * pasa. - Si especifica targetId == sucursal activa del principal → OWN_BRANCH. - Si especifica
   * targetId distinto → ALL_BRANCHES (necesita el permiso más amplio).
   */
  private Alcance resolverAlcanceRequerido(
      @Nullable Serializable targetId, UserPrincipal principal) {
    if (targetId == null) {
      return Alcance.OWN_BRANCH;
    }
    UUID target = toUuid(targetId);
    if (target == null) {
      return Alcance.OWN_BRANCH;
    }
    if (target.equals(principal.sucursalActiva())) {
      return Alcance.OWN_BRANCH;
    }
    return Alcance.ALL_BRANCHES;
  }

  private static @Nullable UserPrincipal principalDe(@Nullable Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }
    Object p = authentication.getPrincipal();
    return p instanceof UserPrincipal up ? up : null;
  }

  private static @Nullable Accion parseAccion(Object permission) {
    try {
      return Accion.valueOf(permission.toString().trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private static @Nullable UUID toUuid(Object value) {
    if (value instanceof UUID u) {
      return u;
    }
    try {
      return UUID.fromString(value.toString());
    } catch (Exception e) {
      return null;
    }
  }
}
