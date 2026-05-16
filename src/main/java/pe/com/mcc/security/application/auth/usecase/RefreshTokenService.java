package pe.com.mcc.security.application.auth.usecase;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcc.security.application.auth.port.in.RefreshTokenCommand;
import pe.com.mcc.security.application.auth.port.in.RefreshTokenUseCase;
import pe.com.mcc.security.application.permission.port.in.BuildPermissionMapUseCase;
import pe.com.mcc.security.application.token.port.in.IssueTokenPairCommand;
import pe.com.mcc.security.application.token.port.in.IssueTokenUseCase;
import pe.com.mcc.security.application.token.port.in.ValidateTokenUseCase;
import pe.com.mcc.security.application.token.port.out.TokenRepository;
import pe.com.mcc.security.application.user.port.out.UserRepository;
import pe.com.mcc.security.domain.auth.exception.InvalidCredentialsException;
import pe.com.mcc.security.domain.permission.model.PermissionMap;
import pe.com.mcc.security.domain.token.exception.TokenInvalidException;
import pe.com.mcc.security.domain.token.model.JwtClaims;
import pe.com.mcc.security.domain.token.model.MotivoRevocacion;
import pe.com.mcc.security.domain.token.model.TipoToken;
import pe.com.mcc.security.domain.token.model.TokenPair;
import pe.com.mcc.security.domain.user.model.SucursalUsuario;
import pe.com.mcc.security.domain.user.model.Usuario;

/**
 * Refresh-token rotation: valida el refresh, lo marca REFRESH_USED y emite un nuevo par bajo la
 * MISMA sesion_id. De esta forma un logout futuro sigue revocando todo en una sola sentencia.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenUseCase {

  private final ValidateTokenUseCase validateToken;
  private final UserRepository userRepository;
  private final BuildPermissionMapUseCase buildPermissionMap;
  private final IssueTokenUseCase issueToken;
  private final TokenRepository tokenRepository;

  @Override
  @Transactional
  public TokenPair refresh(RefreshTokenCommand command) {
    JwtClaims claims = validateToken.validate(command.refreshJwt());

    if (claims.tipo() != TipoToken.REFRESH) {
      throw new TokenInvalidException("Se esperaba un refresh token");
    }

    Usuario usuario =
        userRepository.findById(claims.usuarioId()).orElseThrow(InvalidCredentialsException::new);

    // Rotación: invalidamos toda la sesión anterior y emitimos nueva bajo la misma sesionId.
    tokenRepository.revokeBySesionId(claims.sesionId(), MotivoRevocacion.REFRESH_USED);

    List<SucursalUsuario> sucursales = userRepository.findSucursalesByUsuarioId(usuario.id());
    UUID sucursalActiva =
        claims.sucursalActiva() != null
            ? claims.sucursalActiva()
            : sucursales.stream()
                .filter(SucursalUsuario::esPredeterminada)
                .map(SucursalUsuario::sucursalId)
                .findFirst()
                .orElse(null);
    PermissionMap permisos = buildPermissionMap.build(usuario.id());

    return issueToken.issuePair(
        new IssueTokenPairCommand(
            usuario,
            usuario.empresaId(),
            sucursalActiva,
            sucursales.stream().map(SucursalUsuario::sucursalId).toList(),
            permisos,
            command.dispositivo(),
            claims.sesionId()));
  }
}
