package pe.com.mcc.security.infrastructure.adapter.out.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.ratelimit.port.out.RateLimitStore;
import pe.com.mcc.security.domain.ratelimit.model.RateLimitDecision;
import pe.com.mcc.security.domain.ratelimit.model.RateLimitPolicy;

/**
 * Implementación in-memory con Bucket4j 8.x + Caffeine.
 *
 * <p>SEQ_01: cada policy tiene su propio Caffeine cache de buckets, indexado por la key. Dos
 * políticas distintas con la misma key tienen buckets independientes — bien. SEQ_02: la cache
 * expira buckets que no se tocan en el doble del refillPeriod. Suficiente para liberar memoria de
 * IPs que no han llegado por un tiempo. SEQ_03: tryConsume(1) atomic en Bucket4j — sin race
 * conditions. SEQ_04: cuando deny, Bucket4j devuelve nanosToWaitForRefill, lo convertimos a
 * segundos (techo) para el header Retry-After.
 *
 * <p>Para cluster multi-instancia, sustituir esta impl por una basada en Redis:
 * com.bucket4j:bucket4j-redis. El usecase y el filter no cambian.
 */
@Component
public class Bucket4jRateLimitAdapter implements RateLimitStore {

  private final ConcurrentHashMap<String, Cache<String, Bucket>> cachesByPolicy =
      new ConcurrentHashMap<>();

  @Override
  public RateLimitDecision tryConsume(RateLimitPolicy policy, String key) {
    Bucket bucket = bucketFor(policy, key);
    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

    if (probe.isConsumed()) {
      return RateLimitDecision.allowed(probe.getRemainingTokens());
    }

    long retryAfterSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
    if (retryAfterSeconds == 0) {
      retryAfterSeconds = 1;
    }
    return RateLimitDecision.denied(retryAfterSeconds);
  }

  private Bucket bucketFor(RateLimitPolicy policy, String key) {
    // SEQ_01
    Cache<String, Bucket> cache =
        cachesByPolicy.computeIfAbsent(
            policy.name(),
            p ->
                Caffeine.newBuilder()
                    // SEQ_02
                    .expireAfterAccess(idleTtl(policy.refillPeriod()))
                    .build());

    return cache.get(key, k -> newBucket(policy));
  }

  private Bucket newBucket(RateLimitPolicy policy) {
    // Bucket4j 8.10+ builder fluent. refillIntervally = refill discreto al final
    // de cada período (5 tokens cada 1 min exacto), opuesto a refillGreedy que
    // es continuo proporcional. Para anti brute-force, intervally es más estricto.
    Bandwidth bandwidth =
        Bandwidth.builder()
            .capacity(policy.capacity())
            .refillIntervally(policy.capacity(), policy.refillPeriod())
            .build();
    return Bucket.builder().addLimit(bandwidth).build();
  }

  private static Duration idleTtl(Duration refillPeriod) {
    // 2x el período asegura que un cliente que vuelve antes del próximo refill
    // sigue encontrando su bucket; el resto se libera de la cache.
    return refillPeriod.multipliedBy(2);
  }
}
