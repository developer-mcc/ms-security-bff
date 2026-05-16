package pe.com.mcc.security.infrastructure.adapter.in.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Defensa en profundidad post-logout.
 *
 * <p>Spring Security en modo STATELESS ya limpia el SecurityContextHolder al final del filter chain
 * (vía SecurityContextHolderFilter). Este interceptor garantiza adicionalmente que, después de
 * /auth/logout y /auth/logout-all, el ThreadLocal del SecurityContext quede limpio incluso si: - el
 * filter chain se modifica en el futuro, - se introduce un post-procesamiento síncrono que lea el
 * principal, - se cambia a una política de sesión stateful.
 *
 * <p>Se registra solo para los paths de logout (no global) — no añade overhead a los demás
 * endpoints.
 *
 * <p>afterCompletion se ejecuta SIEMPRE: tanto si el handler devolvió 204 como si lanzó excepción.
 * Esto evita un thread con principal residual aunque el logout haya fallado a mitad de camino.
 */
@Slf4j
@Component
public class LogoutContextCleanupInterceptor implements HandlerInterceptor {

  @Override
  public void afterCompletion(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull Object handler,
      Exception ex) {
    SecurityContextHolder.clearContext();
    log.trace("SecurityContext limpiado post-logout en {}", request.getRequestURI());
  }
}
