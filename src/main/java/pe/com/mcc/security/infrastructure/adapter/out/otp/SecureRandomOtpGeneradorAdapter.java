package pe.com.mcc.security.infrastructure.adapter.out.otp;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.otp.port.out.OtpGenerador;

@Component
public class SecureRandomOtpGeneradorAdapter implements OtpGenerador {

  private static final int DIGITOS = 6;
  private static final int MODULO = 1_000_000;

  private final SecureRandom secureRandom = new SecureRandom();

  @Override
  public String generar() {
    int valor = secureRandom.nextInt(MODULO);
    return String.format("%0" + DIGITOS + "d", valor);
  }
}
