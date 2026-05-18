package pe.com.mcc.security.infrastructure.adapter.out.persistence.user;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.user.port.out.UserRepository;
import pe.com.mcc.security.domain.user.model.SucursalUsuario;
import pe.com.mcc.security.domain.user.model.Usuario;
import pe.com.mcc.security.infrastructure.adapter.out.persistence.permission.RolJpaEntity;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepository {

  private final UsuarioJpaRepository usuarioJpaRepository;
  private final UsuarioMapper usuarioMapper;
  private final EntityManager entityManager;

  @Override
  public Optional<Usuario> findByNombreUsuario(String nombreUsuario) {
    return usuarioJpaRepository.findByNombreUsuario(nombreUsuario).map(usuarioMapper::toDomain);
  }

  @Override
  public Optional<Usuario> findByCorreo(String correo) {
    return usuarioJpaRepository.findByCorreo(correo).map(usuarioMapper::toDomain);
  }

  @Override
  public Optional<Usuario> findByTelefono(String telefono) {
    return usuarioJpaRepository.findByTelefono(telefono).map(usuarioMapper::toDomain);
  }

  @Override
  public Optional<Usuario> findById(UUID usuarioId) {
    return usuarioJpaRepository.findById(usuarioId).map(usuarioMapper::toDomain);
  }

  @Override
  public List<Usuario> findByEmpresaId(UUID empresaId) {
    return usuarioJpaRepository.findByEmpresaId(empresaId).stream()
        .map(usuarioMapper::toDomain)
        .toList();
  }

  @Override
  public Usuario create(Usuario u, List<UUID> sucursalesHabilitadas) {
    UsuarioJpaEntity entity = new UsuarioJpaEntity();
    entity.setId(u.id());
    entity.setEmpresaId(u.empresaId());
    entity.setNombreUsuario(u.nombreUsuario());
    entity.setCorreo(u.correo());
    entity.setContrasenaHash(u.contrasenaHash());
    entity.setNombres(u.nombres());
    entity.setApellidos(u.apellidos());
    entity.setDni(u.dni());
    entity.setTelefono(u.telefono());
    entity.setCanalOtpPreferido(u.canalOtpPreferido().name());
    entity.setMfaHabilitado(u.mfaHabilitado());
    entity.setEstado(u.estado().name());

    UsuarioJpaEntity persisted = usuarioJpaRepository.save(entity);

    if (u.rolesIds() != null) {
      for (String rolId : u.rolesIds()) {
        UsuarioRolJpaEntity ur = new UsuarioRolJpaEntity();
        ur.setId(new UsuarioRolId(persisted.getId(), rolId));
        ur.setUsuario(persisted);
        ur.setRol(entityManager.getReference(RolJpaEntity.class, rolId));
        persisted.getRoles().add(ur);
      }
    }
    if (sucursalesHabilitadas != null) {
      boolean primera = true;
      for (UUID sucursalId : sucursalesHabilitadas) {
        UsuarioSucursalJpaEntity us = new UsuarioSucursalJpaEntity();
        us.setId(new UsuarioSucursalId(persisted.getId(), sucursalId));
        us.setUsuario(persisted);
        us.setSucursal(entityManager.getReference(SucursalJpaEntity.class, sucursalId));
        us.setEsPredeterminada(primera);
        primera = false;
        persisted.getSucursales().add(us);
      }
    }

    return usuarioMapper.toDomain(usuarioJpaRepository.save(persisted));
  }

  @Override
  public void save(Usuario usuario) {
    UsuarioJpaEntity entity =
        usuarioJpaRepository
            .findById(usuario.id())
            .orElseThrow(
                () -> new IllegalStateException("Usuario no existe en BD: " + usuario.id()));
    usuarioMapper.copyMutableFieldsToEntity(usuario, entity);
    usuarioJpaRepository.save(entity);
  }

  @Override
  public void actualizarContrasenaHash(UUID usuarioId, String hash, LocalDateTime ahora) {
    usuarioJpaRepository.actualizarContrasenaHash(usuarioId, hash, ahora);
  }

  @Override
  public List<SucursalUsuario> findSucursalesByUsuarioId(UUID usuarioId) {
    return usuarioJpaRepository
        .findById(usuarioId)
        .map(
            u ->
                u.getSucursales().stream()
                    .map(
                        us ->
                            new SucursalUsuario(
                                us.getSucursal().getId(),
                                us.getSucursal().getNombre(),
                                us.isEsPredeterminada()))
                    .toList())
        .orElse(List.of());
  }
}
