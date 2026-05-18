package pe.com.mcc.security.infrastructure.adapter.out.persistence.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, UUID> {

  Optional<UsuarioJpaEntity> findByNombreUsuario(String nombreUsuario);

  Optional<UsuarioJpaEntity> findByCorreo(String correo);

  Optional<UsuarioJpaEntity> findByTelefono(String telefono);

  List<UsuarioJpaEntity> findByEmpresaId(UUID empresaId);

  @Transactional
  @Modifying
  @Query(
      """
      UPDATE UsuarioJpaEntity u
         SET u.contrasenaHash      = :hash,
             u.contrasenaCambiadaEn = :ahora
       WHERE u.id = :id
      """)
  void actualizarContrasenaHash(
      @Param("id") UUID id, @Param("hash") String hash, @Param("ahora") LocalDateTime ahora);
}
