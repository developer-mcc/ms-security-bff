package pe.com.mcc.security.infrastructure.adapter.in.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.com.mcc.security.application.token.port.in.ValidateTokenUseCase;
import pe.com.mcc.security.domain.token.exception.TokenInvalidException;
import pe.com.mcc.security.domain.token.model.JwtClaims;
import pe.com.mcc.security.domain.token.model.TipoToken;
import pe.com.mcc.security.infrastructure.adapter.out.security.jwt.UserPrincipal;

/**
 * Filtro de autenticación por JWT. Para cada request: 1. Extrae "Authorization: Bearer <jwt>". 2.
 * Delega la validación al ValidateTokenUseCase (firma + exp + jti vs sec.tokens). 3. Si OK y
 * tipo=ACCESS, construye UserPrincipal y popula SecurityContext. 4. Si falla, deja la cadena seguir
 * sin autenticación: el ExceptionTranslationFilter responderá 401 vía AuthenticationEntryPoint.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final ValidateTokenUseCase validateToken;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain chain)
      throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (header != null
        && header.startsWith(BEARER_PREFIX)
        && SecurityContextHolder.getContext().getAuthentication() == null) {
      String jwt = header.substring(BEARER_PREFIX.length());
      try {
        JwtClaims claims = validateToken.validate(jwt);
        if (claims.tipo() == TipoToken.ACCESS) {
          autenticar(claims, request);
        }
      } catch (TokenInvalidException ignored) {
        // sigue sin autenticación
      }
    }

    chain.doFilter(request, response);
  }

  private void autenticar(JwtClaims claims, HttpServletRequest request) {
    UserPrincipal principal =
        new UserPrincipal(
            claims.usuarioId(),
            claims.nombreUsuario(),
            claims.empresaId(),
            claims.sucursalActiva(),
            claims.sesionId(),
            claims.roles(),
            claims.permisos(),
            claims.sucursalesHabilitadas());

    List<SimpleGrantedAuthority> authorities =
        claims.roles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();

    var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }
}
