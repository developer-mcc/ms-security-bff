package pe.com.mcc.security.application.otp.port.out;

import java.util.Optional;
import java.util.UUID;
import pe.com.mcc.security.domain.otp.model.CodigoOtp;
import pe.com.mcc.security.domain.otp.model.PropositoOtp;

public interface OtpRepository {

  CodigoOtp guardar(CodigoOtp otp);

  /** Busca el OTP activo (no expirado, no usado) con bloqueo pesimista para verificación. */
  Optional<CodigoOtp> buscarParaVerificar(UUID usuarioId, PropositoOtp proposito);

  /** Invalida todos los OTPs pendientes anteriores del mismo usuario y propósito. */
  void invalidarPendientes(UUID usuarioId, PropositoOtp proposito);

  /** Purga registros ya usados o expirados. Llamado por el scheduler periódico. */
  int eliminarExpiradosYUsados();
}
