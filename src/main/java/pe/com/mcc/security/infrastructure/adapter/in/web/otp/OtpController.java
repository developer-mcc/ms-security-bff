package pe.com.mcc.security.infrastructure.adapter.in.web.otp;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.com.mcc.security.application.otp.port.in.SolicitarOtpUseCase;
import pe.com.mcc.security.application.otp.port.in.VerificarOtpUseCase;
import pe.com.mcc.security.infrastructure.adapter.in.web.otp.dto.SolicitarOtpRequest;
import pe.com.mcc.security.infrastructure.adapter.in.web.otp.dto.VerificarOtpRequest;
import pe.com.mcc.security.infrastructure.adapter.in.web.otp.mapper.OtpDtoMapper;
import pe.com.mcc.security.infrastructure.adapter.in.web.shared.HttpRequestContextResolver;
import pe.com.mcc.security.infrastructure.adapter.out.security.jwt.UserPrincipal;

@NullMarked
@RestController
@RequestMapping("/auth/otp")
@RequiredArgsConstructor
public class OtpController {

  private final SolicitarOtpUseCase solicitarOtp;
  private final VerificarOtpUseCase verificarOtp;
  private final OtpDtoMapper mapper;
  private final HttpRequestContextResolver requestContext;

  @PostMapping("/request")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> request(
      @Valid @RequestBody SolicitarOtpRequest body,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest request) {
    String ip = requestContext.resolveDispositivo(request).direccionIp();
    solicitarOtp.solicitar(mapper.toCommand(principal.usuarioId(), body, ip));
    return ResponseEntity.accepted().build();
  }

  @PostMapping("/verify")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> verify(
      @Valid @RequestBody VerificarOtpRequest body,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest request) {
    String ip = requestContext.resolveDispositivo(request).direccionIp();
    verificarOtp.verificar(mapper.toCommand(principal.usuarioId(), body, ip));
    return ResponseEntity.noContent().build();
  }
}
