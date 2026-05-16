package pe.com.mcc.security.infrastructure.adapter.out.notification.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Registers state-transition loggers on each notification circuit breaker. All sizing
 * (slidingWindowSize, failureRateThreshold, etc.) is in {@code application.yaml}.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class NotificationResilienceConfig {

  private static final List<String> CB_INSTANCES =
      List.of("email-notification", "sms-notification", "whatsapp-notification");

  private final CircuitBreakerRegistry circuitBreakerRegistry;

  @PostConstruct
  void registerStateTransitionLoggers() {
    CB_INSTANCES.forEach(
        name ->
            circuitBreakerRegistry
                .circuitBreaker(name)
                .getEventPublisher()
                .onStateTransition(
                    e ->
                        log.warn(
                            "CircuitBreaker [{}] {} → {}",
                            name,
                            e.getStateTransition().getFromState(),
                            e.getStateTransition().getToState())));
  }
}
