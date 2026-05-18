package pe.com.mcc.security.application.auth.strategy;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.user.port.out.UserRepository;
import pe.com.mcc.security.domain.user.model.CanalOtp;
import pe.com.mcc.security.domain.user.model.Usuario;

@Component
@RequiredArgsConstructor
public class TelefonoContactoLookupStrategy implements ContactoLookupStrategy {

  private final UserRepository userRepository;

  @Override
  public boolean soporta(CanalOtp canal) {
    return canal == CanalOtp.SMS || canal == CanalOtp.WHATSAPP;
  }

  @Override
  public Optional<Usuario> buscar(String contacto) {
    return userRepository.findByTelefono(contacto);
  }
}
