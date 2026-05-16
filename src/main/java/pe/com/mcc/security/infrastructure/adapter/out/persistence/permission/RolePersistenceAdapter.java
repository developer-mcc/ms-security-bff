package pe.com.mcc.security.infrastructure.adapter.out.persistence.permission;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.permission.port.out.RolRepository;
import pe.com.mcc.security.domain.permission.model.Accion;
import pe.com.mcc.security.domain.permission.model.Alcance;
import pe.com.mcc.security.domain.permission.model.Permiso;

@Component
@RequiredArgsConstructor
public class RolePersistenceAdapter implements RolRepository {

  private final PermisoRolRecursoJpaRepository permisoRepository;

  @Override
  public List<Permiso> findPermisosByUsuarioId(UUID usuarioId) {
    return permisoRepository.findByUsuarioId(usuarioId).stream().map(this::toDomain).toList();
  }

  private Permiso toDomain(PermisoRolRecursoJpaEntity e) {
    Set<Accion> acciones =
        Arrays.stream(e.getAcciones()).map(Accion::valueOf).collect(Collectors.toUnmodifiableSet());
    return new Permiso(e.getRecurso(), acciones, Alcance.valueOf(e.getAlcance()));
  }
}
