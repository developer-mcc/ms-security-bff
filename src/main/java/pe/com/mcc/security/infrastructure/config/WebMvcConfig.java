package pe.com.mcc.security.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pe.com.mcc.security.infrastructure.adapter.in.security.LogoutContextCleanupInterceptor;

/**
 * Configuración Spring MVC. Por ahora solo registra interceptors path-scoped.
 *
 * <p>El LogoutContextCleanupInterceptor solo aplica a /auth/logout y /auth/logout-all para no
 * incurrir en overhead en endpoints que no lo necesitan.
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

  private final LogoutContextCleanupInterceptor logoutContextCleanupInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(logoutContextCleanupInterceptor)
        .addPathPatterns("/auth/logout", "/auth/logout-all");
  }
}
