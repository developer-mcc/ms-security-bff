package pe.com.mcc.security.infrastructure.adapter.out.persistence.auth;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ResetSesionJpaRepository extends JpaRepository<ResetSesionJpaEntity, UUID> {

  @Transactional
  @Modifying
  @Query(
      "DELETE FROM ResetSesionJpaEntity r"
          + " WHERE r.usuarioId = :usuarioId AND r.estado = 'PENDIENTE'")
  void deletePendientesByUsuarioId(@Param("usuarioId") UUID usuarioId);
}
