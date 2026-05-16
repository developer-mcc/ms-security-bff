package pe.com.mcc.security.application.shared.port.out;

import java.time.Instant;
import java.time.LocalDateTime;

/** Abstrae java.time.Clock para que los casos de uso sean testeables (clocks fijos en tests). */
public interface Clock {
  LocalDateTime now();

  Instant nowInstant();
}
