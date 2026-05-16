package pe.com.mcc.security.infrastructure.adapter.in.web.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.com.mcc.security.application.auth.port.in.AuthenticateUseCase;
import pe.com.mcc.security.application.auth.port.in.LogoutUseCase;
import pe.com.mcc.security.application.auth.port.in.RefreshTokenUseCase;
import pe.com.mcc.security.application.auth.port.in.SwitchBranchUseCase;
import pe.com.mcc.security.application.user.port.in.ObtenerPerfilUseCase;
import pe.com.mcc.security.domain.user.model.PerfilUsuario;
import pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto.LoginRequest;
import pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto.PerfilResponse;
import pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto.RefreshRequest;
import pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto.TokenResponse;
import pe.com.mcc.security.infrastructure.adapter.in.web.auth.mapper.AuthDtoMapper;
import pe.com.mcc.security.infrastructure.adapter.in.web.shared.HttpRequestContextResolver;
import pe.com.mcc.security.infrastructure.adapter.out.security.jwt.UserPrincipal;

/**
 * Controller thin: solo orquesta. La lectura de IP/UA y el armado de DispositivoInfo se delega a
 * HttpRequestContextResolver. La transformación DTO<->command/response a AuthDtoMapper.
 */
@NullMarked
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthenticateUseCase authenticate;
  private final RefreshTokenUseCase refreshToken;
  private final LogoutUseCase logout;
  private final SwitchBranchUseCase switchBranch;
  private final ObtenerPerfilUseCase obtenerPerfil;
  private final AuthDtoMapper mapper;
  private final HttpRequestContextResolver requestContext;

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(
      @Valid @RequestBody LoginRequest body, HttpServletRequest request) {
    var command = mapper.toCommand(body, requestContext.resolveDispositivo(request));
    var pair = authenticate.authenticate(command);
    return ResponseEntity.ok(mapper.toResponse(pair));
  }

  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> refresh(
      @Valid @RequestBody RefreshRequest body, HttpServletRequest request) {
    var command = mapper.toCommand(body, requestContext.resolveDispositivo(request));
    var pair = refreshToken.refresh(command);
    return ResponseEntity.ok(mapper.toResponse(pair));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@AuthenticationPrincipal UserPrincipal principal) {
    logout.logout(principal.sesionId());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/logout-all")
  public ResponseEntity<Void> logoutAll(@AuthenticationPrincipal UserPrincipal principal) {
    logout.logoutAll(principal.usuarioId());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/switch-branch/{sucursalId}")
  public ResponseEntity<TokenResponse> switchBranch(
      @PathVariable UUID sucursalId,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest request) {
    var command =
        mapper.toSwitchBranchCommand(
            principal, sucursalId, requestContext.resolveDispositivo(request));
    var pair = switchBranch.switchBranch(command);
    return ResponseEntity.ok(mapper.toResponse(pair));
  }

  @GetMapping("/me")
  public ResponseEntity<PerfilResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
    PerfilUsuario perfil =
        obtenerPerfil.obtenerPerfil(principal.usuarioId(), principal.sucursalActiva());
    return ResponseEntity.ok(mapper.toPerfilResponse(perfil));
  }
}
