package pe.com.mcc.security.infrastructure.adapter.in.web.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.com.mcc.security.application.auth.port.in.AuthenticateUseCase;
import pe.com.mcc.security.application.auth.port.in.CambiarContrasenaCommand;
import pe.com.mcc.security.application.auth.port.in.CambiarContrasenaUseCase;
import pe.com.mcc.security.application.auth.port.in.CompletarMfaUseCase;
import pe.com.mcc.security.application.auth.port.in.LogoutUseCase;
import pe.com.mcc.security.application.auth.port.in.RefreshTokenUseCase;
import pe.com.mcc.security.application.auth.port.in.ResetPasswordCommand;
import pe.com.mcc.security.application.auth.port.in.ResetPasswordUseCase;
import pe.com.mcc.security.application.auth.port.in.SolicitarResetCommand;
import pe.com.mcc.security.application.auth.port.in.SolicitarResetUseCase;
import pe.com.mcc.security.application.auth.port.in.SwitchBranchUseCase;
import pe.com.mcc.security.application.auth.port.in.VerificarResetOtpCommand;
import pe.com.mcc.security.application.auth.port.in.VerificarResetOtpUseCase;
import pe.com.mcc.security.application.user.port.in.ObtenerPerfilUseCase;
import pe.com.mcc.security.domain.user.model.PerfilUsuario;
import pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto.CambiarContrasenaRequest;
import pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto.LoginMfaVerifyRequest;
import pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto.LoginRequest;
import pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto.PerfilResponse;
import pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto.RefreshRequest;
import pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto.ResetPasswordAnonimRequest;
import pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto.SolicitarResetRequest;
import pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto.SolicitarResetResponse;
import pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto.TokenResponse;
import pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto.VerificarResetOtpRequest;
import pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto.VerificarResetOtpResponse;
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
  private final CompletarMfaUseCase completarMfa;
  private final RefreshTokenUseCase refreshToken;
  private final LogoutUseCase logout;
  private final SwitchBranchUseCase switchBranch;
  private final ObtenerPerfilUseCase obtenerPerfil;
  private final CambiarContrasenaUseCase cambiarContrasena;
  private final SolicitarResetUseCase solicitarReset;
  private final VerificarResetOtpUseCase verificarResetOtp;
  private final ResetPasswordUseCase resetPassword;
  private final AuthDtoMapper mapper;
  private final HttpRequestContextResolver requestContext;

  /**
   * Autentica por contraseña. Sin MFA retorna 200 con tokens; con MFA retorna 202 con preAuthToken
   * y canal para continuar en POST /auth/login/mfa/verify.
   */
  @PostMapping("/login")
  public ResponseEntity<?> login(
      @Valid @RequestBody LoginRequest body, HttpServletRequest request) {
    return mapper.toLoginResponse(
        authenticate.authenticate(
            mapper.toCommand(body, requestContext.resolveDispositivo(request))));
  }

  /** Segundo factor: verifica el OTP recibido por el canal preferido y emite los tokens finales. */
  @PostMapping("/login/mfa/verify")
  public ResponseEntity<TokenResponse> verifyMfa(
      @Valid @RequestBody LoginMfaVerifyRequest body, HttpServletRequest request) {
    var command = mapper.toCompletarMfaCommand(body, requestContext.resolveDispositivo(request));
    var pair = completarMfa.completar(command);
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

  /**
   * Cambia la contraseña del usuario autenticado. Requiere haber completado el flujo OTP con
   * proposito=RESET_PASSWORD antes de llamar este endpoint. Revoca todos los tokens activos al
   * finalizar — el cliente debe limpiar su sesión local y redirigir al login.
   */
  @PatchMapping("/password")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> cambiarContrasena(
      @Valid @RequestBody CambiarContrasenaRequest body,
      @AuthenticationPrincipal UserPrincipal principal) {
    cambiarContrasena.cambiarContrasena(
        new CambiarContrasenaCommand(principal.usuarioId(), body.nuevaContrasena()));
    return ResponseEntity.noContent().build();
  }

  /**
   * Paso 1 — inicia el flujo de recuperación de contraseña sin autenticación. Genera un OTP y lo
   * envía al canal elegido. Retorna 202 siempre para evitar enumeración de cuentas.
   */
  @PostMapping("/password/forgot")
  public ResponseEntity<SolicitarResetResponse> forgotPassword(
      @Valid @RequestBody SolicitarResetRequest body, HttpServletRequest request) {
    var result =
        solicitarReset.solicitar(
            new SolicitarResetCommand(
                body.contacto(),
                body.canal(),
                requestContext.resolveDispositivo(request).direccionIp()));
    return ResponseEntity.accepted().body(mapper.toSolicitarResetResponse(result));
  }

  /**
   * Paso 2 — verifica el OTP del flujo forgot-password. Retorna un resetToken de un solo uso que
   * autoriza el PATCH /auth/password/reset.
   */
  @PostMapping("/password/forgot/verify")
  public ResponseEntity<VerificarResetOtpResponse> forgotPasswordVerify(
      @Valid @RequestBody VerificarResetOtpRequest body, HttpServletRequest request) {
    var result =
        verificarResetOtp.verificar(
            new VerificarResetOtpCommand(
                body.preResetToken(),
                body.codigo(),
                requestContext.resolveDispositivo(request).direccionIp()));
    return ResponseEntity.ok(new VerificarResetOtpResponse(result.resetToken()));
  }

  /**
   * Paso 3 — establece la nueva contraseña usando el resetToken obtenido en el paso 2. Revoca todos
   * los tokens activos del usuario — el cliente debe redirigir al login.
   */
  @PatchMapping("/password/reset")
  public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordAnonimRequest body) {
    resetPassword.reset(new ResetPasswordCommand(body.resetToken(), body.nuevaContrasena()));
    return ResponseEntity.noContent().build();
  }
}
