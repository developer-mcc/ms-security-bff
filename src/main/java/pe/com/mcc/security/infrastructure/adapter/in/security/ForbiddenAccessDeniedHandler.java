package pe.com.mcc.security.infrastructure.adapter.in.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Maneja 403 cuando el JWT es válido pero el rol/permiso no autoriza el endpoint. Aplica a
 * denegaciones del filter chain (autorizaciones path-based de authorizeHttpRequests). Las negativas
 * vía @PreAuthorize las atrapa el GlobalExceptionHandler.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ForbiddenAccessDeniedHandler implements AccessDeniedHandler {

  private final ProblemDetailWriter writer;

  @Override
  public void handle(
      HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
      throws IOException {
    log.debug("403 en {}: {}", request.getRequestURI(), ex.getMessage());
    writer.write(
        response,
        HttpStatus.FORBIDDEN,
        "access-denied",
        "Permiso insuficiente",
        "El rol del usuario no autoriza esta operación.");
  }
}
