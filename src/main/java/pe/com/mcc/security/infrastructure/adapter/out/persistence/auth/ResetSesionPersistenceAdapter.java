package pe.com.mcc.security.infrastructure.adapter.out.persistence.auth;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.auth.port.out.ResetSesionRepository;
import pe.com.mcc.security.domain.auth.model.EstadoResetSesion;
import pe.com.mcc.security.domain.auth.model.ResetSesion;

@Component
@RequiredArgsConstructor
public class ResetSesionPersistenceAdapter implements ResetSesionRepository {

  private final ResetSesionJpaRepository repository;

  @Override
  public void guardar(ResetSesion sesion) {
    ResetSesionJpaEntity entity =
        repository.findById(sesion.id()).orElseGet(ResetSesionJpaEntity::new);
    entity.setId(sesion.id());
    entity.setUsuarioId(sesion.usuarioId());
    entity.setEstado(sesion.estado().name());
    entity.setExpiraEn(sesion.expiraEn());
    repository.save(entity);
  }

  @Override
  public Optional<ResetSesion> buscarPorId(UUID id) {
    return repository.findById(id).map(this::toDomain);
  }

  @Override
  public void eliminarPendientes(UUID usuarioId) {
    repository.deletePendientesByUsuarioId(usuarioId);
  }

  private ResetSesion toDomain(ResetSesionJpaEntity e) {
    return new ResetSesion(
        e.getId(), e.getUsuarioId(), EstadoResetSesion.valueOf(e.getEstado()), e.getExpiraEn());
  }
}
