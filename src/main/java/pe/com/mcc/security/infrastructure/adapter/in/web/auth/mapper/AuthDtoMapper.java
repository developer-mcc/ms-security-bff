package pe.com.mcc.security.infrastructure.adapter.in.web.auth.mapper;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.auth.port.in.AuthenticateCommand;
import pe.com.mcc.security.application.auth.port.in.CompletarMfaCommand;
import pe.com.mcc.security.application.auth.port.in.RefreshTokenCommand;
import pe.com.mcc.security.application.auth.port.in.SolicitarResetResult;
import pe.com.mcc.security.application.auth.port.in.SwitchBranchCommand;
import pe.com.mcc.security.domain.auth.model.AuthenticateResult;
import pe.com.mcc.security.domain.auth.model.Credentials;
import pe.com.mcc.security.domain.auth.model.DispositivoInfo;
import pe.com.mcc.security.domain.token.model.TokenPair;
import pe.com.mcc.security.domain.user.model.PerfilUsuario;
import pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto.LoginMfaVerifyRequest;
import pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto.LoginRequest;
import pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto.MfaRequeridoResponse;
import pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto.PerfilResponse;
import pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto.RefreshRequest;
import pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto.SolicitarResetResponse;
import pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto.SucursalInfo;
import pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto.TokenResponse;
import pe.com.mcc.security.infrastructure.adapter.out.security.jwt.UserPrincipal;

@NullMarked
@Component
public class AuthDtoMapper {

  public AuthenticateCommand toCommand(LoginRequest body, DispositivoInfo dispositivo) {
    return new AuthenticateCommand(
        new Credentials(body.nombreUsuario(), body.contrasena()), dispositivo);
  }

  public RefreshTokenCommand toCommand(RefreshRequest body, DispositivoInfo dispositivo) {
    return new RefreshTokenCommand(body.refreshToken(), dispositivo);
  }

  public SwitchBranchCommand toSwitchBranchCommand(
      UserPrincipal principal, UUID sucursalNueva, DispositivoInfo dispositivo) {
    return new SwitchBranchCommand(
        principal.usuarioId(),
        principal.sesionId(),
        principal.sucursalActiva(),
        sucursalNueva,
        dispositivo);
  }

  public TokenResponse toResponse(TokenPair pair) {
    return new TokenResponse(
        pair.sesionId(),
        pair.access().jwt(),
        pair.access().expiraEn(),
        pair.refresh().jwt(),
        pair.refresh().expiraEn(),
        "Bearer");
  }

  public SolicitarResetResponse toSolicitarResetResponse(SolicitarResetResult result) {
    List<SolicitarResetResponse.CanalInfo> canales =
        result.canalesDisponibles().stream()
            .map(c -> new SolicitarResetResponse.CanalInfo(c.tipo(), c.valorMascarado()))
            .toList();
    return new SolicitarResetResponse(result.preResetToken(), canales);
  }

  public ResponseEntity<?> toLoginResponse(AuthenticateResult result) {
    return switch (result) {
      case AuthenticateResult.TokenEmitido(var pair) -> ResponseEntity.ok(toResponse(pair));
      case AuthenticateResult.MfaRequerido(var preAuthToken, var canal) ->
          ResponseEntity.accepted().body(new MfaRequeridoResponse(preAuthToken, canal.name()));
    };
  }

  public CompletarMfaCommand toCompletarMfaCommand(
      LoginMfaVerifyRequest body, DispositivoInfo dispositivo) {
    return new CompletarMfaCommand(body.preAuthToken(), body.codigo(), dispositivo);
  }

  public PerfilResponse toPerfilResponse(PerfilUsuario perfil) {
    List<SucursalInfo> sucursales =
        perfil.sucursalesDisponibles().stream()
            .map(s -> new SucursalInfo(s.sucursalId(), s.nombre(), s.esPredeterminada()))
            .toList();
    return new PerfilResponse(
        perfil.id(),
        perfil.empresaId(),
        perfil.nombreUsuario(),
        perfil.correo(),
        perfil.nombres(),
        perfil.apellidos(),
        perfil.dni(),
        perfil.telefono(),
        perfil.canalOtpPreferido().name(),
        perfil.mfaHabilitado(),
        perfil.rol(),
        perfil.sucursalActiva(),
        perfil.sucursalActivaNombre(),
        sucursales);
  }
}
