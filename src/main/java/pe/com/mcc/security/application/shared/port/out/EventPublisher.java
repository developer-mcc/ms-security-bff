package pe.com.mcc.security.application.shared.port.out;

/**
 * Wrapper sobre ApplicationEventPublisher de Spring. Mantiene el dominio libre de dependencias del
 * framework y permite tests sin contexto Spring.
 */
public interface EventPublisher {
  void publish(Object event);
}
