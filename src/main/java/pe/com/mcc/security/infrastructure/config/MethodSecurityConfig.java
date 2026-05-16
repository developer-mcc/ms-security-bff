package pe.com.mcc.security.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import pe.com.mcc.security.infrastructure.adapter.out.security.permission.ThreeDPermissionEvaluator;

/**
 * Wire del PermissionEvaluator personalizado.
 *
 * <p>Spring Security 6 lee este bean cuando @EnableMethodSecurity está activo:
 * cualquier @PreAuthorize que use hasPermission(...) lo invocará.
 *
 * <p>{@code @Role(ROLE_INFRASTRUCTURE)} registra el bean como infraestructura — evita resolver
 * dependencias durante el bootstrap del contexto de seguridad.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class MethodSecurityConfig {

  private final ThreeDPermissionEvaluator permissionEvaluator;

  @Bean
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
    handler.setPermissionEvaluator(permissionEvaluator);
    return handler;
  }
}
