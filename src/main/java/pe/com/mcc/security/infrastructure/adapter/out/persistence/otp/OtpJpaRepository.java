package pe.com.mcc.security.infrastructure.adapter.out.persistence.otp;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

interface OtpJpaRepository extends JpaRepository<CodigoOtpJpaEntity, UUID> {

  /**
   * Bloqueo pesimista para prevenir condiciones de carrera en verificaciones concurrentes. Solo
   * busca OTPs no expirados y no usados.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT o FROM CodigoOtpJpaEntity o "
          + "WHERE o.usuarioId = :usuarioId AND o.proposito = :proposito "
          + "AND o.expiraEn > :ahora AND o.usadoEn IS NULL "
          + "ORDER BY o.fechaCreacion DESC")
  Optional<CodigoOtpJpaEntity> findParaVerificar(
      @Param("usuarioId") UUID usuarioId,
      @Param("proposito") String proposito,
      @Param("ahora") LocalDateTime ahora);

  @Transactional
  @Modifying
  @Query(
      "UPDATE CodigoOtpJpaEntity o SET o.usadoEn = :ahora "
          + "WHERE o.usuarioId = :usuarioId AND o.proposito = :proposito "
          + "AND o.usadoEn IS NULL")
  void invalidarPendientes(
      @Param("usuarioId") UUID usuarioId,
      @Param("proposito") String proposito,
      @Param("ahora") LocalDateTime ahora);

  @Transactional
  @Modifying
  @Query("DELETE FROM CodigoOtpJpaEntity o " + "WHERE o.usadoEn IS NOT NULL OR o.expiraEn < :ahora")
  int eliminarExpiradosYUsados(@Param("ahora") LocalDateTime ahora);
}
