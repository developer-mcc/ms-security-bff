package pe.com.mcc.security.infrastructure.adapter.in.security;

/**
 * Claves MDC compartidas por el filtro y por los listeners. Mantenerlas como constantes evita typos
 * que impidan correlación de logs.
 */
public final class MdcConstants {

  public static final String TRACE_ID = "traceId";
  public static final String SPAN_ID = "spanId";
  public static final String USER_ID = "userId";
  public static final String USERNAME = "username";
  public static final String EMPRESA_ID = "empresaId";
  public static final String SUCURSAL_ID = "sucursalId";

  public static final String HEADER_REQUEST_ID = "X-Request-Id";
  public static final String HEADER_CORRELATION_ID = "X-Correlation-Id";

  private MdcConstants() {}
}
