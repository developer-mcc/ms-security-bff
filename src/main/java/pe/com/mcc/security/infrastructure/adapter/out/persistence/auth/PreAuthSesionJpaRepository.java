package pe.com.mcc.security.infrastructure.adapter.out.persistence.auth;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PreAuthSesionJpaRepository extends JpaRepository<PreAuthSesionJpaEntity, UUID> {

  @Transactional
  @Modifying
  @Query(
      "DELETE FROM PreAuthSesionJpaEntity p"
          + " WHERE p.usuarioId = :usuarioId AND p.estado = 'PENDIENTE'")
  void deletePendientesByUsuarioId(@Param("usuarioId") UUID usuarioId);
}
