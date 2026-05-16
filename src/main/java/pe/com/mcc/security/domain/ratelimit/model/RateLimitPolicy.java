package pe.com.mcc.security.domain.ratelimit.model;

import java.time.Duration;

/**
 * Política de rate-limit aplicable a un endpoint. - name: identificador legible (ej "login",
 * "otp-verify"). - capacity: máximo de requests permitidos en el período. - refillPeriod: ventana
 * sobre la que se refrescan los tokens (1 min, 5 min, etc). - keyedBy: dimensión sobre la que se
 * acumulan los tokens.
 */
public record RateLimitPolicy(String name, int capacity, Duration refillPeriod, KeyedBy keyedBy) {
  public RateLimitPolicy {
    if (capacity <= 0) {
      throw new IllegalArgumentException("capacity > 0");
    }
    if (refillPeriod == null || refillPeriod.isZero() || refillPeriod.isNegative()) {
      throw new IllegalArgumentException("refillPeriod debe ser positivo");
    }
  }
}
