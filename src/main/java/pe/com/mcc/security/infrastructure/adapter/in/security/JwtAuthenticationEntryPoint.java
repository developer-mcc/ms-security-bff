package pe.com.mcc.security.infrastructure.adapter.in.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Maneja 401 cuando un request a un endpoint protegido no trae JWT (o el JWT fue rechazado por el
 * filter). Sin este bean, Spring Security responde 401 con body vacío.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ProblemDetailWriter writer;

  @Override
  public void commence(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
      throws IOException {
    log.debug("401 en {}: {}", request.getRequestURI(), ex.getMessage());
    writer.write(
        response,
        HttpStatus.UNAUTHORIZED,
        "authentication-required",
        "Autenticación requerida",
        "Falta token o el token entregado no es válido.");
  }
}
