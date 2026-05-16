package pe.com.mcc.security.infrastructure.adapter.out.persistence.user;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.notification.port.out.ContactoUsuario;
import pe.com.mcc.security.application.notification.port.out.ContactoUsuarioPort;
import pe.com.mcc.security.domain.user.model.CanalOtp;

@Component
@RequiredArgsConstructor
public class ContactoUsuarioPersistenceAdapter implements ContactoUsuarioPort {

  private final UsuarioJpaRepository repository;

  @Override
  public Optional<ContactoUsuario> buscarContacto(UUID usuarioId) {
    return repository
        .findById(usuarioId)
        .map(
            e ->
                new ContactoUsuario(
                    e.getCorreo(), e.getTelefono(), CanalOtp.valueOf(e.getCanalOtpPreferido())));
  }
}
