package pe.com.mcc.security.infrastructure.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.com.mcc.security.domain.tenant.model.TenantInfo;
import pe.com.mcc.security.infrastructure.adapter.in.security.MdcConstants;
import pe.com.mcc.security.infrastructure.adapter.out.security.jwt.UserPrincipal;

/**
 * Resuelve el TenantInfo (empresa_id + sucursal_id activa) desde el UserPrincipal que dejó el
 * JwtAuthenticationFilter en el SecurityContext, y lo deposita en el TenantContext (ThreadLocal).
 *
 * <p>Además enriquece el MDC con userId/username/empresaId/sucursalId para que los logs
 * estructurados los emitan en cada línea. El MdcFilter limpia todo en finally, por lo que aquí no
 * se hace MDC.remove() explícito.
 *
 * <p>Se registra DESPUÉS del JwtAuthenticationFilter para asegurar que el principal ya está
 * poblado. Limpia el TenantContext en finally para evitar fugas entre requests.
 */
@Component
public class TenantResolverFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain chain)
      throws ServletException, IOException {
    try {
      poblarSiAutenticado();
      chain.doFilter(request, response);
    } finally {
      TenantContext.clear();
    }
  }

  private void poblarSiAutenticado() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      return;
    }
    if (!(auth.getPrincipal() instanceof UserPrincipal up)) {
      return;
    }

    TenantContext.set(new TenantInfo(up.empresaId(), up.sucursalActiva()));

    putMdc(MdcConstants.USER_ID, up.usuarioId());
    putMdc(MdcConstants.USERNAME, up.nombreUsuario());
    putMdc(MdcConstants.EMPRESA_ID, up.empresaId());
    putMdc(MdcConstants.SUCURSAL_ID, up.sucursalActiva());
  }

  private static void putMdc(String key, Object value) {
    if (value == null) {
      return;
    }
    MDC.put(key, value instanceof UUID u ? u.toString() : value.toString());
  }
}
