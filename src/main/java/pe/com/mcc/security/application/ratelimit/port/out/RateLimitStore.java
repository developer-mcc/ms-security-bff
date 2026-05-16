package pe.com.mcc.security.application.ratelimit.port.out;

import pe.com.mcc.security.domain.ratelimit.model.RateLimitDecision;
import pe.com.mcc.security.domain.ratelimit.model.RateLimitPolicy;

/**
 * Puerto de salida. Implementación por defecto: Bucket4jRateLimitAdapter (Caffeine in-memory).
 * Cambiar a Redis en producción multi-instancia es solo cambiar la impl, no el caso de uso.
 */
public interface RateLimitStore {

  /**
   * Intenta consumir 1 token de la bucket identificada por (policy, key). Si no hay tokens
   * disponibles, devuelve denied con los segundos hasta el próximo refill.
   */
  RateLimitDecision tryConsume(RateLimitPolicy policy, String key);
}
