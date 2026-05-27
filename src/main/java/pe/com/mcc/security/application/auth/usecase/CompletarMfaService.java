package pe.com.mcc.security.application.auth.usecase;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcc.security.application.auth.port.in.CompletarMfaCommand;
import pe.com.mcc.security.application.auth.port.in.CompletarMfaUseCase;
import pe.com.mcc.security.application.auth.port.out.PreAuthSesionRepository;
import pe.com.mcc.security.application.otp.port.in.VerificarOtpCommand;
import pe.com.mcc.security.application.otp.port.in.VerificarOtpUseCase;
import pe.com.mcc.security.application.permission.port.in.BuildPermissionMapUseCase;
import pe.com.mcc.security.application.shared.port.out.Clock;
import pe.com.mcc.security.application.shared.port.out.EventPublisher;
import pe.com.mcc.security.application.token.port.in.IssueTokenPairCommand;
import pe.com.mcc.security.application.token.port.in.IssueTokenUseCase;
import pe.com.mcc.security.application.user.port.out.UserRepository;
import pe.com.mcc.security.domain.auth.event.LoginSuccessEvent;
import pe.com.mcc.security.domain.auth.exception.PreAuthSesionInvalidaException;
import pe.com.mcc.security.domain.auth.model.PreAuthSesion;
import pe.com.mcc.security.domain.otp.exception.OtpExpiradoException;
import pe.com.mcc.security.domain.otp.exception.OtpInvalidoException;
import pe.com.mcc.security.domain.otp.exception.OtpMaxIntentosException;
import pe.com.mcc.security.domain.otp.model.PropositoOtp;
import pe.com.mcc.security.domain.permission.model.PermissionMap;
import pe.com.mcc.security.domain.token.model.TokenPair;
import pe.com.mcc.security.domain.user.model.SucursalUsuario;
import pe.com.mcc.security.domain.user.model.Usuario;

/**
 * Segundo paso del flujo MFA: valida el preAuthToken y el OTP, aplica conLoginExitoso al usuario,
 * re-resuelve permisos y emite el TokenPair final. El noRollbackFor asegura que el contador de
 * intentos del OTP sobreviva al throw — mismo patrón que VerificarResetOtpService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompletarMfaService implements CompletarMfaUseCase {

  private final PreAuthSesionRepository preAuthSesionRepository;
  private final UserRepository userRepository;
  private final VerificarOtpUseCase verificarOtp;
  private final BuildPermissionMapUseCase buildPermissionMap;
  private final IssueTokenUseCase issueToken;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  @Override
  @Transactional(
      noRollbackFor = {
        OtpInvalidoException.class,
        OtpExpiradoException.class,
        OtpMaxIntentosException.class
      })
  public TokenPair completar(CompletarMfaCommand command) {
    PreAuthSesion sesion =
        preAuthSesionRepository
            .buscarPorId(command.preAuthToken())
            .orElseThrow(
                () -> new PreAuthSesionInvalidaException("Token MFA inválido o no encontrado."));

    if (!sesion.estaActiva(clock.now())) {
      throw new PreAuthSesionInvalidaException(
          "El token MFA ha expirado. Inicia sesión nuevamente.");
    }

    String ip = command.dispositivo().direccionIp();

    verificarOtp.verificar(
        new VerificarOtpCommand(sesion.usuarioId(), command.codigo(), PropositoOtp.LOGIN_2FA, ip));

    preAuthSesionRepository.guardar(sesion.usada());

    Usuario usuario =
        userRepository
            .findById(sesion.usuarioId())
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Usuario no encontrado tras MFA: " + sesion.usuarioId()));

    Usuario actualizado = usuario.conLoginExitoso(clock.now());
    userRepository.save(actualizado);

    List<SucursalUsuario> sucursales = userRepository.findSucursalesByUsuarioId(actualizado.id());
    UUID sucursalActiva =
        sucursales.stream()
            .filter(SucursalUsuario::esPredeterminada)
            .map(SucursalUsuario::sucursalId)
            .findFirst()
            .orElse(null);

    PermissionMap permisos = buildPermissionMap.build(actualizado.id());

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
            ip,
            clock.nowInstant()));

    log.debug("MFA completado usuario={} sesion={}", actualizado.id(), pair.sesionId());
    return pair;
  }
}
