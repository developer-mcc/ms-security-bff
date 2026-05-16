package pe.com.mcc.security.infrastructure.adapter.out.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.token.port.out.TokenSigner;
import pe.com.mcc.security.domain.permission.model.Permiso;
import pe.com.mcc.security.domain.token.model.JwtClaims;
import pe.com.mcc.security.domain.token.model.TipoToken;
import pe.com.mcc.security.infrastructure.config.JwtProperties;

/**
 * Firma JWT con HS256 (HMAC-SHA256). El secret se carga desde JwtProperties. El claim "perms" se
 * serializa como Map<String, {actions:[], scope:""}> — el cliente Angular lo lee directo y el
 * ThreeDPermissionEvaluator también.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenSignerAdapter implements TokenSigner {

  private final JwtProperties props;

  @Override
  public String sign(JwtClaims claims) {
    var builder =
        Jwts.builder()
            .id(claims.jti().toString())
            .issuer(props.issuer())
            .subject(claims.usuarioId().toString())
            .issuedAt(Date.from(claims.emitidoEn().atZone(ZoneId.systemDefault()).toInstant()))
            .expiration(Date.from(claims.expiraEn().atZone(ZoneId.systemDefault()).toInstant()))
            .claim("sid", claims.sesionId().toString())
            .claim("tipo", claims.tipo().name())
            .claim("usr", claims.nombreUsuario())
            .claim("emp", claims.empresaId() != null ? claims.empresaId().toString() : null)
            .claim(
                "suc", claims.sucursalActiva() != null ? claims.sucursalActiva().toString() : null)
            .claim("roles", claims.roles());

    if (claims.tipo() == TipoToken.ACCESS) {
      builder
          .claim("perms", serializePermisos(claims))
          .claim("sucs", claims.sucursalesHabilitadas().stream().map(UUID::toString).toList());
    }

    return builder.signWith(secretKey()).compact();
  }

  private Map<String, Object> serializePermisos(JwtClaims claims) {
    Map<String, Object> map = new HashMap<>();
    for (Map.Entry<String, Permiso> e : claims.permisos().asMap().entrySet()) {
      Permiso p = e.getValue();
      map.put(
          e.getKey(),
          Map.of(
              "actions", p.acciones().stream().map(Enum::name).toList(),
              "scope", p.alcance().name()));
    }
    return map;
  }

  private SecretKey secretKey() {
    return Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
  }
}
