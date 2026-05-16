package pe.com.mcc.security.infrastructure.adapter.out.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.shared.port.out.EventPublisher;

@Component
@RequiredArgsConstructor
public class EventPublisherAdapter implements EventPublisher {

  private final ApplicationEventPublisher delegate;

  @Override
  public void publish(Object event) {
    delegate.publishEvent(event);
  }
}
