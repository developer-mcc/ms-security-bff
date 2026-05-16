package pe.com.mcc.security.application.token.usecase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcc.security.application.shared.port.out.Clock;
import pe.com.mcc.security.application.shared.port.out.IdGenerator;
import pe.com.mcc.security.application.token.port.in.IssueTokenPairCommand;
import pe.com.mcc.security.application.token.port.in.IssueTokenUseCase;
import pe.com.mcc.security.application.token.port.out.TokenRepository;
import pe.com.mcc.security.application.token.port.out.TokenSigner;
import pe.com.mcc.security.domain.permission.model.PermissionMap;
import pe.com.mcc.security.domain.token.model.JwtClaims;
import pe.com.mcc.security.domain.token.model.TipoToken;
import pe.com.mcc.security.domain.token.model.Token;
import pe.com.mcc.security.domain.token.model.TokenFirmado;
import pe.com.mcc.security.domain.token.model.TokenPair;
import pe.com.mcc.security.domain.user.model.Usuario;

/**
 * Emite un par access+refresh con la misma sesion_id. Persiste ambos JTI en sec.tokens antes de
 * devolver los JWT firmados — así el JwtAuthenticationFilter podrá validarlos inmediatamente en la
 * siguiente request.
 *
 * <p>SRP: este servicio solo emite tokens. No valida credenciales (eso es AuthenticateService) ni
 * permisos (eso es BuildPermissionMapService).
 */
@Service
@RequiredArgsConstructor
public class IssueTokenService implements IssueTokenUseCase {

  private final TokenSigner tokenSigner;
  private final TokenRepository tokenRepository;
  private final IdGenerator idGenerator;
  private final Clock clock;

  @Value("${security.jwt.access-ttl-minutes:15}")
  private long accessTtlMinutes;

  @Value("${security.jwt.refresh-ttl-minutes:10080}") // 7 días
  private long refreshTtlMinutes;

  @Override
  @Transactional
  public TokenPair issuePair(IssueTokenPairCommand command) {
    UUID sesionId = command.sesionIdReuso() != null ? command.sesionIdReuso() : idGenerator.newId();
    LocalDateTime ahora = clock.now();

    TokenFirmado access =
        emitir(
            command,
            sesionId,
            TipoToken.ACCESS,
            ahora.plusMinutes(accessTtlMinutes),
            ahora,
            command.permisos());
    TokenFirmado refresh =
        emitir(
            command,
            sesionId,
            TipoToken.REFRESH,
            ahora.plusMinutes(refreshTtlMinutes),
            ahora,
            PermissionMap.empty());

    return new TokenPair(sesionId, access, refresh);
  }

  private TokenFirmado emitir(
      IssueTokenPairCommand cmd,
      UUID sesionId,
      TipoToken tipo,
      LocalDateTime expiraEn,
      LocalDateTime ahora,
      PermissionMap permisos) {
    UUID jti = idGenerator.newId();
    Usuario u = cmd.usuario();

    Token token =
        new Token(
            jti,
            sesionId,
            u.id(),
            cmd.empresaId(),
            cmd.sucursalActiva(),
            tipo,
            null,
            cmd.dispositivo() != null ? cmd.dispositivo().huellaDispositivo() : null,
            cmd.dispositivo() != null ? cmd.dispositivo().direccionIp() : null,
            cmd.dispositivo() != null ? cmd.dispositivo().agenteUsuario() : null,
            ahora,
            expiraEn,
            false,
            null,
            null);
    tokenRepository.save(token);

    JwtClaims claims =
        new JwtClaims(
            jti,
            sesionId,
            tipo,
            u.id(),
            u.nombreUsuario(),
            cmd.empresaId(),
            cmd.sucursalActiva(),
            u.rolesIds() != null ? u.rolesIds() : List.of(),
            permisos,
            cmd.sucursalesHabilitadas() != null ? cmd.sucursalesHabilitadas() : List.of(),
            ahora,
            expiraEn);
    String jwt = tokenSigner.sign(claims);
    return new TokenFirmado(jti, tipo, jwt, expiraEn);
  }
}
