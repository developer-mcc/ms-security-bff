package pe.com.mcc.security.domain.token.model;

import java.util.UUID;

/** Par access + refresh emitidos en una misma sesión. Ambos comparten sesionId. */
public record TokenPair(UUID sesionId, TokenFirmado access, TokenFirmado refresh) {}
