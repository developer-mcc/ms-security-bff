package pe.com.mcc.security.domain.ratelimit.model;

/**
 * Veredicto del rate-limiter para una request concreta. - allowed: false ⇒ devolver 429. -
 * retryAfterSeconds: header HTTP Retry-After. - remainingTokens: para cabeceras informativas
 * X-RateLimit-Remaining.
 */
public record RateLimitDecision(boolean allowed, long retryAfterSeconds, long remainingTokens) {

  public static RateLimitDecision allowed(long remaining) {
    return new RateLimitDecision(true, 0, remaining);
  }

  public static RateLimitDecision denied(long retryAfterSeconds) {
    return new RateLimitDecision(false, retryAfterSeconds, 0);
  }
}
