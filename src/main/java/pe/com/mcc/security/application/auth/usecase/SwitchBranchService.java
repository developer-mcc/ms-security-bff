package pe.com.mcc.security.application.auth.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcc.security.application.auth.port.in.SwitchBranchCommand;
import pe.com.mcc.security.application.auth.port.in.SwitchBranchUseCase;
import pe.com.mcc.security.application.permission.port.in.BuildPermissionMapUseCase;
import pe.com.mcc.security.application.shared.port.out.Clock;
import pe.com.mcc.security.application.shared.port.out.EventPublisher;
import pe.com.mcc.security.application.token.port.in.IssueTokenPairCommand;
import pe.com.mcc.security.application.token.port.in.IssueTokenUseCase;
import pe.com.mcc.security.application.token.port.out.TokenRepository;
import pe.com.mcc.security.application.user.port.out.UserRepository;
import pe.com.mcc.security.domain.auth.event.BranchSwitchedEvent;
import pe.com.mcc.security.domain.auth.exception.InvalidCredentialsException;
import pe.com.mcc.security.domain.auth.exception.SucursalNoAutorizadaException;
import pe.com.mcc.security.domain.permission.model.PermissionMap;
import pe.com.mcc.security.domain.token.model.MotivoRevocacion;
import pe.com.mcc.security.domain.token.model.TokenPair;
import pe.com.mcc.security.domain.user.model.SucursalUsuario;
import pe.com.mcc.security.domain.user.model.Usuario;

/**
 * Cambio de sucursal activa sin re-loguear.
 *
 * <p>Pasos: 1) Validar que la sucursal solicitada está en usuarios_sucursales del usuario. 2)
 * Revocar TODA la sesión actual (BRANCH_SWITCH) — invalida access + refresh viejos. 3) Reconstruir
 * PermissionMap (los permisos no cambian, pero el alcance sí se evalúa relativo a la sucursal
 * activa nueva). 4) Emitir un par nuevo bajo NUEVA sesion_id con sucursal_id actualizada. 5)
 * Publicar BranchSwitchedEvent (para auditoría).
 *
 * <p>Nota de diseño: NO se reutiliza la sesion_id anterior. Switch-branch es un cambio de contexto
 * explícito; tener una sesion_id distinta facilita correlacionar eventos de auditoría con el
 * "antes" y el "después".
 */
@Service
@RequiredArgsConstructor
public class SwitchBranchService implements SwitchBranchUseCase {

  private final UserRepository userRepository;
  private final TokenRepository tokenRepository;
  private final BuildPermissionMapUseCase buildPermissionMap;
  private final IssueTokenUseCase issueToken;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  @Override
  @Transactional
  public TokenPair switchBranch(SwitchBranchCommand command) {
    Usuario usuario =
        userRepository.findById(command.usuarioId()).orElseThrow(InvalidCredentialsException::new);

    List<SucursalUsuario> sucursales = userRepository.findSucursalesByUsuarioId(usuario.id());
    boolean autorizado =
        sucursales.stream().anyMatch(s -> s.sucursalId().equals(command.sucursalNueva()));
    if (!autorizado) {
      throw new SucursalNoAutorizadaException(command.sucursalNueva());
    }

    tokenRepository.revokeBySesionId(command.sesionIdActual(), MotivoRevocacion.BRANCH_SWITCH);

    PermissionMap permisos = buildPermissionMap.build(usuario.id());

    TokenPair pair =
        issueToken.issuePair(
            new IssueTokenPairCommand(
                usuario,
                usuario.empresaId(),
                command.sucursalNueva(),
                sucursales.stream().map(SucursalUsuario::sucursalId).toList(),
                permisos,
                command.dispositivo(),
                null // nueva sesion_id — switch-branch es un corte de contexto
                ));

    eventPublisher.publish(
        new BranchSwitchedEvent(
            usuario.id(),
            usuario.nombreUsuario(),
            usuario.empresaId(),
            command.sesionIdActual(),
            pair.sesionId(),
            command.sucursalActual(),
            command.sucursalNueva(),
            command.dispositivo() != null ? command.dispositivo().direccionIp() : null,
            clock.nowInstant()));

    return pair;
  }
}
