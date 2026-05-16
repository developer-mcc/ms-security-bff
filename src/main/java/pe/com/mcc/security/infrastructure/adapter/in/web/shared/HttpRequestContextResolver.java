package pe.com.mcc.security.infrastructure.adapter.in.web.shared;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.domain.auth.model.DispositivoInfo;

/**
 * Único componente con acceso al HttpServletRequest dentro del flujo /auth/*. Centraliza la
 * extracción de IP, User-Agent y huella, devolviendo el VO de dominio. Mantiene los controllers
 * libres de lógica.
 */
@Component
public class HttpRequestContextResolver {

  private static final String HEADER_FINGERPRINT = "X-Device-Fingerprint";
  private static final String HEADER_FORWARDED = "X-Forwarded-For";

  public DispositivoInfo resolveDispositivo(HttpServletRequest request) {
    return new DispositivoInfo(
        resolveIp(request), request.getHeader("User-Agent"), request.getHeader(HEADER_FINGERPRINT));
  }

  private String resolveIp(HttpServletRequest request) {
    String forwarded = request.getHeader(HEADER_FORWARDED);
    if (forwarded != null && !forwarded.isBlank()) {
      int comma = forwarded.indexOf(',');
      return comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
    }
    return request.getRemoteAddr();
  }
}
