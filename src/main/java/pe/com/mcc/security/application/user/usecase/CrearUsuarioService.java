package pe.com.mcc.security.application.user.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcc.security.application.auth.port.out.PasswordEncoderPort;
import pe.com.mcc.security.application.shared.port.out.Clock;
import pe.com.mcc.security.application.shared.port.out.IdGenerator;
import pe.com.mcc.security.application.user.port.in.CrearUsuarioCommand;
import pe.com.mcc.security.application.user.port.in.CrearUsuarioUseCase;
import pe.com.mcc.security.application.user.port.out.UserRepository;
import pe.com.mcc.security.domain.user.model.EstadoUsuario;
import pe.com.mcc.security.domain.user.model.Usuario;

@Service
@RequiredArgsConstructor
public class CrearUsuarioService implements CrearUsuarioUseCase {

  private final UserRepository userRepository;
  private final PasswordEncoderPort passwordEncoder;
  private final IdGenerator idGenerator;
  private final Clock clock;

  @Override
  @Transactional
  public Usuario crear(CrearUsuarioCommand cmd) {
    Usuario nuevo =
        new Usuario(
            idGenerator.newId(),
            cmd.empresaId(),
            cmd.nombreUsuario(),
            cmd.correo(),
            passwordEncoder.encode(cmd.contrasena()),
            cmd.nombres(),
            cmd.apellidos(),
            cmd.dni(),
            cmd.telefono(),
            cmd.canalOtpPreferido() != null
                ? cmd.canalOtpPreferido()
                : pe.com.mcc.security.domain.user.model.CanalOtp.EMAIL,
            cmd.mfaHabilitado(),
            EstadoUsuario.ACTIVO,
            0,
            null,
            null,
            cmd.rolesIds());
    return userRepository.create(nuevo, cmd.sucursalesHabilitadas());
  }
}
