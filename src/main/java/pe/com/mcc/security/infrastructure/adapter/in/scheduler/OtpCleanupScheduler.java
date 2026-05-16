package pe.com.mcc.security.infrastructure.adapter.in.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.otp.port.out.OtpRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtpCleanupScheduler {

  private final OtpRepository otpRepository;

  /** Purga OTPs expirados o ya usados cada 15 minutos. */
  @Scheduled(fixedDelayString = "${security.otp.cleanup-interval-ms:900000}")
  public void purgarOtpsExpirados() {
    int eliminados = otpRepository.eliminarExpiradosYUsados();
    if (eliminados > 0) {
      log.info("OTP cleanup: {} registros eliminados", eliminados);
    }
  }
}
