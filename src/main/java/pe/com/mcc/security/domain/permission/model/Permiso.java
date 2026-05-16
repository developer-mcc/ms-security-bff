package pe.com.mcc.security.domain.permission.model;

import java.util.Set;

/** Una entrada del modelo 3D: (recurso, acciones[], alcance). */
public record Permiso(String recurso, Set<Accion> acciones, Alcance alcance) {

  public boolean cubre(String recursoConsulta, Accion accion, Alcance alcanceRequerido) {
    boolean recursoOk =
        Recurso.WILDCARD.equals(this.recurso) || this.recurso.equals(recursoConsulta);
    boolean accionOk = acciones.contains(accion);
    boolean alcanceOk = this.alcance == Alcance.ALL_BRANCHES || this.alcance == alcanceRequerido;
    return recursoOk && accionOk && alcanceOk;
  }
}
