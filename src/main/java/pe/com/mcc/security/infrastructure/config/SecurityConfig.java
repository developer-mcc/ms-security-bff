package pe.com.mcc.security.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pe.com.mcc.security.infrastructure.adapter.in.security.ForbiddenAccessDeniedHandler;
import pe.com.mcc.security.infrastructure.adapter.in.security.JwtAuthenticationEntryPoint;
import pe.com.mcc.security.infrastructure.adapter.in.security.JwtAuthenticationFilter;
import pe.com.mcc.security.infrastructure.adapter.in.security.MdcFilter;
import pe.com.mcc.security.infrastructure.adapter.in.security.RateLimitFilter;
import pe.com.mcc.security.infrastructure.tenant.TenantResolverFilter;

// @EnableMethodSecurity vive en MethodSecurityConfig junto con el PermissionEvaluator 3D.

@Configuration
@EnableConfigurationProperties({JwtProperties.class, RateLimitProperties.class})
@RequiredArgsConstructor
public class SecurityConfig {

  private final MdcFilter mdcFilter;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final TenantResolverFilter tenantResolverFilter;
  private final RateLimitFilter rateLimitFilter;
  private final JwtAuthenticationEntryPoint authenticationEntryPoint;
  private final ForbiddenAccessDeniedHandler accessDeniedHandler;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        // Las denegaciones del filter chain se serializan como ProblemDetail
        // (mismo formato que las del @RestControllerAdvice).
        .exceptionHandling(
            eh ->
                eh.authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.POST, "/auth/login", "/auth/refresh")
                    .permitAll()
                    .requestMatchers("/actuator/health", "/actuator/info")
                    .permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        // Orden de ejecución (todos anclados antes de UsernamePasswordAuthenticationFilter,
        // que es el único filter "estándar" referenciable en Spring Security 6):
        //   1) MDC          — traceId/spanId disponibles en todos los logs siguientes
        //   2) JWT          — popula SecurityContext con UserPrincipal
        //   3) TenantResolver — pobla TenantContext + MDC con userId/empresaId
        //   4) RateLimit    — DESPUÉS del JWT para que policies USER_ID lean el principal
        .addFilterBefore(mdcFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(tenantResolverFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
