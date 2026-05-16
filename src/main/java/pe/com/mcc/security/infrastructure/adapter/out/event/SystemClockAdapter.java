package pe.com.mcc.security.infrastructure.adapter.out.event;

import java.time.Instant;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.shared.port.out.Clock;

@Component
@RequiredArgsConstructor
public class SystemClockAdapter implements Clock {

  private final java.time.Clock clock;

  @Override
  public LocalDateTime now() {
    return LocalDateTime.now(clock);
  }

  @Override
  public Instant nowInstant() {
    return Instant.now(clock);
  }
}
