package pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record SolicitarResetResponse(UUID preResetToken, List<CanalInfo> canalesDisponibles) {

  public record CanalInfo(String tipo, String valorMascarado) {}
}
