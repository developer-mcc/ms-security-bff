package pe.com.mcc.security.application.auth.usecase;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcc.security.application.auth.port.in.AuthenticateCommand;
import pe.com.mcc.security.application.auth.port.in.AuthenticateUseCase;
import pe.com.mcc.security.application.auth.port.in.RegistrarIntentoFallidoUseCase;
import pe.com.mcc.security.application.auth.port.in.SolicitarMfaCommand;
import pe.com.mcc.security.application.auth.port.in.SolicitarMfaUseCase;
import pe.com.mcc.security.application.auth.port.out.PasswordEncoderPort;
import pe.com.mcc.security.application.permission.port.in.BuildPermissionMapUseCase;
import pe.com.mcc.security.application.shared.port.out.Clock;
import pe.com.mcc.security.application.shared.port.out.EventPublisher;
import pe.com.mcc.security.application.token.port.in.IssueTokenPairCommand;
import pe.com.mcc.security.application.token.port.in.IssueTokenUseCase;
import pe.com.mcc.security.application.user.port.out.UserRepository;
import pe.com.mcc.security.domain.auth.event.LoginFailedEvent;
import pe.com.mcc.security.domain.auth.event.LoginSuccessEvent;
import pe.com.mcc.security.domain.auth.exception.InvalidCredentialsException;
import pe.com.mcc.security.domain.auth.exception.UserBlockedException;
import pe.com.mcc.security.domain.auth.model.AuthenticateResult;
import pe.com.mcc.security.domain.permission.model.PermissionMap;
import pe.com.mcc.security.domain.user.model.SucursalUsuario;
import pe.com.mcc.security.domain.user.model.Usuario;

/**
 * Orquesta el flujo de autenticación. Único responsable de: 1) Buscar al usuario y comprobar su
 * estado. 2) Validar la contraseña. 3) Actualizar contadores de intento y bloqueo automático. 4)
 * Resolver la sucursal predeterminada y construir el PermissionMap. 5) Delegar la emisión de tokens
 * al IssueTokenUseCase. 6) Publicar eventos de dominio (LoginSuccess / LoginFailed).
 *
 * <p>No conoce JJWT, ni HTTP, ni Spring Security beyond @Transactional.
 */
@Service
@RequiredArgsConstructor
public class AuthenticateService implements AuthenticateUseCase {

  private final UserRepository userRepository;
  private final PasswordEncoderPort passwordEncoder;
  private final BuildPermissionMapUseCase buildPermissionMap;
  private final IssueTokenUseCase issueToken;
  private final RegistrarIntentoFallidoUseCase registrarIntentoFallido;
  private final SolicitarMfaUseCase solicitarMfa;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  @Override
  @Transactional
  public AuthenticateResult authenticate(AuthenticateCommand command) {
    String nombreUsuario = command.credentials().nombreUsuario();

    Usuario usuario =
        userRepository
            .findByNombreUsuario(nombreUsuario)
            .orElseThrow(
                () -> {
                  publicarFallo(command, "USUARIO_NO_ENCONTRADO");
                  return new InvalidCredentialsException();
                });

    if (usuario.estaBloqueado(clock.now())) {
      publicarFallo(command, "USUARIO_BLOQUEADO");
      throw new UserBlockedException();
    }

    if (!passwordEncoder.matches(command.credentials().contrasena(), usuario.contrasenaHash())) {
      // REQUIRES_NEW: el incremento sobrevive al rollback de la excepción que tiramos abajo.
      registrarIntentoFallido.registrarFallo(usuario.id());
      publicarFallo(command, "PASSWORD_INVALIDO");
      throw new InvalidCredentialsException();
    }

    if (usuario.mfaHabilitado()) {
      String ip = command.dispositivo() != null ? command.dispositivo().direccionIp() : null;
      return solicitarMfa.solicitar(new SolicitarMfaCommand(usuario, ip));
    }

    Usuario actualizado = usuario.conLoginExitoso(clock.now());
    userRepository.save(actualizado);

    List<SucursalUsuario> sucursales = userRepository.findSucursalesByUsuarioId(usuario.id());
    UUID sucursalActiva =
        sucursales.stream()
            .filter(SucursalUsuario::esPredeterminada)
            .map(SucursalUsuario::sucursalId)
            .findFirst()
            .orElse(null);

    PermissionMap permisos = buildPermissionMap.build(usuario.id());

    var pair =
        issueToken.issuePair(
            new IssueTokenPairCommand(
                actualizado,
                actualizado.empresaId(),
                sucursalActiva,
                sucursales.stream().map(SucursalUsuario::sucursalId).toList(),
                permisos,
                command.dispositivo(),
                null));

    eventPublisher.publish(
        new LoginSuccessEvent(
            actualizado.id(),
            actualizado.nombreUsuario(),
            actualizado.empresaId(),
            sucursalActiva,
            pair.sesionId(),
            command.dispositivo() != null ? command.dispositivo().direccionIp() : null,
            clock.nowInstant()));

    return new AuthenticateResult.TokenEmitido(pair);
  }

  private void publicarFallo(AuthenticateCommand command, String motivo) {
    eventPublisher.publish(
        new LoginFailedEvent(
            command.credentials().nombreUsuario(),
            motivo,
            command.dispositivo() != null ? command.dispositivo().direccionIp() : null,
            clock.nowInstant()));
  }
}
