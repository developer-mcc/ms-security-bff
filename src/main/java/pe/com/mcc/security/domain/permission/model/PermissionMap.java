package pe.com.mcc.security.domain.permission.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Mapa serializable a JWT: recurso -> Permiso. El PermissionEvaluator de Spring Security consulta
 * cubre(...) sobre cada entrada.
 */
public final class PermissionMap {

  private final Map<String, Permiso> porRecurso;

  public PermissionMap(Map<String, Permiso> porRecurso) {
    this.porRecurso = Map.copyOf(porRecurso);
  }

  public static PermissionMap empty() {
    return new PermissionMap(Collections.emptyMap());
  }

  public static PermissionMap of(Collection<Permiso> permisos) {
    Map<String, Permiso> map = new HashMap<>();
    for (Permiso p : permisos) {
      map.put(p.recurso(), p);
    }
    return new PermissionMap(map);
  }

  public Map<String, Permiso> asMap() {
    return porRecurso;
  }

  public boolean concede(String recurso, Accion accion, Alcance alcanceRequerido) {
    Permiso wildcard = porRecurso.get(Recurso.WILDCARD);
    if (wildcard != null && wildcard.cubre(recurso, accion, alcanceRequerido)) {
      return true;
    }
    Permiso p = porRecurso.get(recurso);
    return p != null && p.cubre(recurso, accion, alcanceRequerido);
  }
}
