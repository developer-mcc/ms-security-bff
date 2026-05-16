package pe.com.mcc.security.infrastructure.adapter.out.persistence.auth;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface TokenJpaRepository extends JpaRepository<TokenJpaEntity, UUID> {

  @Transactional
  @Modifying
  @Query(
      """
      UPDATE TokenJpaEntity t
         SET t.revocado          = TRUE,
             t.revocadoEn        = :ahora,
             t.motivoRevocacion  = :motivo
       WHERE t.sesionId          = :sesionId
         AND t.revocado          = FALSE
      """)
  int revokeBySesionId(
      @Param("sesionId") UUID sesionId,
      @Param("motivo") String motivo,
      @Param("ahora") LocalDateTime ahora);

  @Transactional
  @Modifying
  @Query(
      """
      UPDATE TokenJpaEntity t
         SET t.revocado          = TRUE,
             t.revocadoEn        = :ahora,
             t.motivoRevocacion  = :motivo
       WHERE t.usuarioId         = :usuarioId
         AND t.revocado          = FALSE
      """)
  int revokeByUsuarioId(
      @Param("usuarioId") UUID usuarioId,
      @Param("motivo") String motivo,
      @Param("ahora") LocalDateTime ahora);
}
