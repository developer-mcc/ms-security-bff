package pe.com.mcc.security.infrastructure.adapter.out.otp;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.otp.port.out.OtpHasher;

@Component
@RequiredArgsConstructor
public class BcryptOtpHasherAdapter implements OtpHasher {

  private final PasswordEncoder passwordEncoder;

  @Override
  public String hashear(String codigoPlano) {
    return passwordEncoder.encode(codigoPlano);
  }

  @Override
  public boolean verificar(String codigoPlano, String hash) {
    return passwordEncoder.matches(codigoPlano, hash);
  }
}
