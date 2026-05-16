package pe.com.mcc.security.domain.token.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * El JWT serializado (firmado) listo para enviar al cliente, junto con metadata útil para construir
 * el response HTTP.
 */
public record TokenFirmado(UUID jti, TipoToken tipo, String jwt, LocalDateTime expiraEn) {}
