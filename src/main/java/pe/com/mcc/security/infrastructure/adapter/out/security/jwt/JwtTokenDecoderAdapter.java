package pe.com.mcc.security.infrastructure.adapter.out.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.token.port.out.TokenDecoder;
import pe.com.mcc.security.domain.permission.model.Accion;
import pe.com.mcc.security.domain.permission.model.Alcance;
import pe.com.mcc.security.domain.permission.model.Permiso;
import pe.com.mcc.security.domain.permission.model.PermissionMap;
import pe.com.mcc.security.domain.token.exception.TokenExpiredException;
import pe.com.mcc.security.domain.token.exception.TokenInvalidException;
import pe.com.mcc.security.domain.token.model.JwtClaims;
import pe.com.mcc.security.domain.token.model.TipoToken;
import pe.com.mcc.security.infrastructure.config.JwtProperties;

@Component
@RequiredArgsConstructor
public class JwtTokenDecoderAdapter implements TokenDecoder {

  private final JwtProperties props;

  @Override
  public JwtClaims decode(String compactJwt) {
    try {
      Claims c =
          Jwts.parser()
              .verifyWith(secretKey())
              .requireIssuer(props.issuer())
              .build()
              .parseSignedClaims(compactJwt)
              .getPayload();

      return toJwtClaims(c);
    } catch (ExpiredJwtException e) {
      throw new TokenExpiredException();
    } catch (JwtException | IllegalArgumentException e) {
      throw new TokenInvalidException("JWT inválido: " + e.getMessage(), e);
    }
  }

  private JwtClaims toJwtClaims(Claims c) {
    TipoToken tipo = TipoToken.valueOf(c.get("tipo", String.class));
    UUID jti = UUID.fromString(c.getId());
    UUID sid = UUID.fromString(c.get("sid", String.class));
    UUID userId = UUID.fromString(c.getSubject());

    UUID empresaId = uuidOrNull(c.get("emp", String.class));
    UUID sucursalActiva = uuidOrNull(c.get("suc", String.class));
    List<String> roles = getStringList(c.get("roles"));

    PermissionMap perms = PermissionMap.empty();
    List<UUID> sucursales = List.of();
    if (tipo == TipoToken.ACCESS) {
      perms = deserializePermisos(c.get("perms"));
      sucursales = getStringList(c.get("sucs")).stream().map(UUID::fromString).toList();
    }

    LocalDateTime emitidoEn =
        c.getIssuedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    LocalDateTime expiraEn =
        c.getExpiration().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

    return new JwtClaims(
        jti,
        sid,
        tipo,
        userId,
        c.get("usr", String.class),
        empresaId,
        sucursalActiva,
        roles,
        perms,
        sucursales,
        emitidoEn,
        expiraEn);
  }

  /**
   * Convierte cualquier valor de claim a List&lt;String&gt; sin casts unchecked. Si el valor no es
   * una lista, devuelve lista vacía (defensa ante claims malformados).
   */
  private static List<String> getStringList(Object raw) {
    if (!(raw instanceof List<?> list)) {
      return List.of();
    }
    return list.stream().map(String::valueOf).toList();
  }

  /**
   * Deserializa el claim "perms" (Map&lt;recurso, {actions:[], scope:""}&gt;). Usa pattern matching
   * instanceof — sin casts unchecked, defensive ante datos malformados.
   */
  private PermissionMap deserializePermisos(Object raw) {
    if (!(raw instanceof java.util.Map<?, ?> outer)) {
      return PermissionMap.empty();
    }
    List<Permiso> list =
        outer.entrySet().stream().map(this::toPermiso).filter(Objects::nonNull).toList();
    return PermissionMap.of(list);
  }

  private Permiso toPermiso(java.util.Map.Entry<?, ?> entry) {
    if (!(entry.getValue() instanceof java.util.Map<?, ?> inner)) {
      return null;
    }
    Set<Accion> acciones = parseAcciones(inner.get("actions"));
    Alcance alcance = Alcance.valueOf(String.valueOf(inner.get("scope")));
    return new Permiso(String.valueOf(entry.getKey()), acciones, alcance);
  }

  private static Set<Accion> parseAcciones(Object raw) {
    if (!(raw instanceof List<?> list)) {
      return Set.of();
    }
    return list.stream()
        .map(o -> Accion.valueOf(String.valueOf(o)))
        .collect(Collectors.toUnmodifiableSet());
  }

  private static UUID uuidOrNull(String s) {
    return s == null || s.isBlank() ? null : UUID.fromString(s);
  }

  private SecretKey secretKey() {
    return Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
  }
}
