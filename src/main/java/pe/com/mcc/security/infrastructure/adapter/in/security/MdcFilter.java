package pe.com.mcc.security.infrastructure.adapter.in.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Pone traceId/spanId en el MDC al entrar la request y los limpia al salir.
 *
 * <p>Reglas de resolución del traceId entrante (en orden): 1. Header X-Request-Id (custom
 * convention). 2. Header X-Correlation-Id (gateways comunes lo emiten). 3. UUID generado (request
 * originado en el front sin trazabilidad upstream).
 *
 * <p>Echo en la respuesta: el mismo header X-Request-Id se devuelve para que el cliente lo
 * correlacione con sus logs y con la bitácora del servidor.
 *
 * <p>Userld/empresaId/sucursalId los completa el TenantResolverFilter después del
 * JwtAuthenticationFilter — aquí solo se ponen los IDs de trazabilidad técnica.
 */
@Component
public class MdcFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain chain)
      throws ServletException, IOException {
    String traceId = resolveTraceId(request);
    String spanId = generateShortId();

    MDC.put(MdcConstants.TRACE_ID, traceId);
    MDC.put(MdcConstants.SPAN_ID, spanId);

    response.setHeader(MdcConstants.HEADER_REQUEST_ID, traceId);

    try {
      chain.doFilter(request, response);
    } finally {
      // Limpia TODO el MDC al final, incluyendo lo que TenantResolverFilter
      // haya puesto. Evita filtraciones entre requests del thread pool.
      MDC.clear();
    }
  }

  private static String resolveTraceId(HttpServletRequest request) {
    String header = request.getHeader(MdcConstants.HEADER_REQUEST_ID);
    if (header == null || header.isBlank()) {
      header = request.getHeader(MdcConstants.HEADER_CORRELATION_ID);
    }
    return (header != null && !header.isBlank()) ? header : generateShortId();
  }

  /** UUID compacto sin guiones — más legible en logs. */
  private static String generateShortId() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
