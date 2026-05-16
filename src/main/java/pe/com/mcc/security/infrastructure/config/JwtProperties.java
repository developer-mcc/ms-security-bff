package pe.com.mcc.security.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades JWT bajo el prefijo security.jwt.* en application.yaml - secret: clave HS256 (mínimo
 * 32 bytes/256 bits). - issuer: identificador del emisor. - access-ttl-minutes /
 * refresh-ttl-minutes: TTL relativos.
 */
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
    String secret, String issuer, long accessTtlMinutes, long refreshTtlMinutes) {}
