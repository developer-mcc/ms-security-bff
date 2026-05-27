package pe.com.mcc.security.infrastructure.adapter.out.persistence.auth;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.auth.port.out.PreAuthSesionRepository;
import pe.com.mcc.security.domain.auth.model.EstadoPreAuthSesion;
import pe.com.mcc.security.domain.auth.model.PreAuthSesion;

@Component
@RequiredArgsConstructor
public class PreAuthSesionPersistenceAdapter implements PreAuthSesionRepository {

  private final PreAuthSesionJpaRepository repository;

  @Override
  public void guardar(PreAuthSesion sesion) {
    PreAuthSesionJpaEntity entity =
        repository.findById(sesion.id()).orElseGet(PreAuthSesionJpaEntity::new);
    entity.setId(sesion.id());
    entity.setUsuarioId(sesion.usuarioId());
    entity.setEmpresaId(sesion.empresaId());
    entity.setEstado(sesion.estado().name());
    entity.setIpOrigen(sesion.ipOrigen());
    entity.setExpiraEn(sesion.expiraEn());
    repository.save(entity);
  }

  @Override
  public Optional<PreAuthSesion> buscarPorId(UUID id) {
    return repository.findById(id).map(this::toDomain);
  }

  @Override
  public void eliminarPendientesPorUsuario(UUID usuarioId) {
    repository.deletePendientesByUsuarioId(usuarioId);
  }

  private PreAuthSesion toDomain(PreAuthSesionJpaEntity e) {
    return new PreAuthSesion(
        e.getId(),
        e.getUsuarioId(),
        e.getEmpresaId(),
        EstadoPreAuthSesion.valueOf(e.getEstado()),
        e.getIpOrigen(),
        e.getExpiraEn());
  }
}
