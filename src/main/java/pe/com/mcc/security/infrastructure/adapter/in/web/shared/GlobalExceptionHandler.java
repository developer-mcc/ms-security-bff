package pe.com.mcc.security.infrastructure.adapter.in.web.shared;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.com.mcc.security.domain.auth.exception.InvalidCredentialsException;
import pe.com.mcc.security.domain.auth.exception.SucursalNoAutorizadaException;
import pe.com.mcc.security.domain.auth.exception.UserBlockedException;
import pe.com.mcc.security.domain.otp.exception.OtpExpiradoException;
import pe.com.mcc.security.domain.otp.exception.OtpInvalidoException;
import pe.com.mcc.security.domain.otp.exception.OtpMaxIntentosException;
import pe.com.mcc.security.domain.token.exception.TokenExpiredException;
import pe.com.mcc.security.domain.token.exception.TokenInvalidException;
import pe.com.mcc.security.domain.token.exception.TokenRevokedException;
import pe.com.mcc.security.infrastructure.adapter.in.security.MdcConstants;

/**
 * Handler central de errores. Todos retornan ProblemDetail (RFC 9457) con: - type:
 * urn:problem-type:<slug> - title, detail, status estándar - traceId: tomado del MDC para
 * correlación con logs del servidor - errors: solo en validation (map field -> mensaje)
 *
 * <p>Las denegaciones del filter chain (401 sin token, 403 antes del controller) las maneja Spring
 * Security vía AuthenticationEntryPoint y AccessDeniedHandler en SecurityConfig. Aquí se atrapan
 * las que llegan al @RestController.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // ============================================================
  // Authentication / token (401)
  // ============================================================

  @ExceptionHandler(InvalidCredentialsException.class)
  public ProblemDetail handleInvalidCredentials(InvalidCredentialsException e) {
    return problem(
        HttpStatus.UNAUTHORIZED, "invalid-credentials", "Credenciales inválidas", e.getMessage());
  }

  @ExceptionHandler(TokenExpiredException.class)
  public ProblemDetail handleExpired(TokenExpiredException e) {
    return problem(HttpStatus.UNAUTHORIZED, "token-expired", "Token expirado", e.getMessage());
  }

  @ExceptionHandler(TokenRevokedException.class)
  public ProblemDetail handleRevoked(TokenRevokedException e) {
    return problem(HttpStatus.UNAUTHORIZED, "token-revoked", "Token revocado", e.getMessage());
  }

  @ExceptionHandler(TokenInvalidException.class)
  public ProblemDetail handleInvalid(TokenInvalidException e) {
    return problem(HttpStatus.UNAUTHORIZED, "token-invalid", "Token inválido", e.getMessage());
  }

  // ============================================================
  // OTP (400 / 422)
  // ============================================================

  @ExceptionHandler(OtpInvalidoException.class)
  public ProblemDetail handleOtpInvalido(OtpInvalidoException e) {
    return problem(HttpStatus.UNPROCESSABLE_ENTITY, "otp-invalido", "OTP inválido", e.getMessage());
  }

  @ExceptionHandler(OtpExpiradoException.class)
  public ProblemDetail handleOtpExpirado(OtpExpiradoException e) {
    return problem(HttpStatus.UNPROCESSABLE_ENTITY, "otp-expirado", "OTP expirado", e.getMessage());
  }

  @ExceptionHandler(OtpMaxIntentosException.class)
  public ProblemDetail handleOtpMaxIntentos(OtpMaxIntentosException e) {
    return problem(
        HttpStatus.UNPROCESSABLE_ENTITY,
        "otp-max-intentos",
        "Máximo de intentos OTP alcanzado",
        e.getMessage());
  }

  // ============================================================
  // Authorization (403)
  // ============================================================

  @ExceptionHandler(UserBlockedException.class)
  public ProblemDetail handleBlocked(UserBlockedException e) {
    return problem(HttpStatus.FORBIDDEN, "user-blocked", "Usuario bloqueado", e.getMessage());
  }

  @ExceptionHandler(SucursalNoAutorizadaException.class)
  public ProblemDetail handleBranchNotAuthorized(SucursalNoAutorizadaException e) {
    return problem(
        HttpStatus.FORBIDDEN,
        "branch-not-authorized",
        "Sucursal no autorizada",
        "El usuario no tiene acceso a la sucursal solicitada.");
  }

  @ExceptionHandler({AuthorizationDeniedException.class, AccessDeniedException.class})
  public ProblemDetail handleAccessDenied(RuntimeException e) {
    log.debug("Acceso denegado: {}", e.getMessage());
    return problem(
        HttpStatus.FORBIDDEN,
        "access-denied",
        "Permiso insuficiente",
        "El rol del usuario no autoriza esta operación.");
  }

  // ============================================================
  // Validation (400)
  // ============================================================

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleBeanValidation(MethodArgumentNotValidException e) {
    Map<String, String> errors = new LinkedHashMap<>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(fe -> errors.put(fe.getField(), fe.getDefaultMessage()));

    ProblemDetail pd =
        problem(
            HttpStatus.BAD_REQUEST,
            "validation-error",
            "Datos de entrada inválidos",
            "Uno o más campos no cumplen las restricciones declarativas.");
    pd.setProperty("errors", errors);
    return pd;
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ProblemDetail handleConstraintViolation(ConstraintViolationException e) {
    Map<String, String> errors = new LinkedHashMap<>();
    for (ConstraintViolation<?> v : e.getConstraintViolations()) {
      errors.put(v.getPropertyPath().toString(), v.getMessage());
    }
    ProblemDetail pd =
        problem(
            HttpStatus.BAD_REQUEST, "validation-error", "Restricciones violadas", e.getMessage());
    pd.setProperty("errors", errors);
    return pd;
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ProblemDetail handleMalformedJson(HttpMessageNotReadableException e) {
    return problem(
        HttpStatus.BAD_REQUEST,
        "malformed-request",
        "JSON inválido o body ausente",
        "El cuerpo de la petición no se pudo deserializar.");
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleIllegalArgument(IllegalArgumentException e) {
    return problem(
        HttpStatus.BAD_REQUEST, "illegal-argument", "Argumento inválido", e.getMessage());
  }

  // ============================================================
  // Catch-all (500)
  // ============================================================

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleAny(Exception e) {
    // Log server-side con stack trace para diagnóstico — el cliente no lo ve.
    log.error("Excepción no manejada: {}", e.getMessage(), e);
    return problem(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "internal-server-error",
        "Error interno del servidor",
        "Ocurrió un error procesando la solicitud. Refiere al traceId para soporte.");
  }

  // ============================================================
  // Helper
  // ============================================================

  private ProblemDetail problem(HttpStatus status, String slug, String title, String detail) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
    pd.setType(URI.create("urn:problem-type:" + slug));
    pd.setTitle(title);
    String traceId = MDC.get(MdcConstants.TRACE_ID);
    if (traceId != null) {
      pd.setProperty("traceId", traceId);
    }
    return pd;
  }
}
