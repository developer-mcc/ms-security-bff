package pe.com.mcc.security.domain.audit.model;

import java.util.UUID;

/**
 * Entrada inmutable de la bitácora. valorAnterior/valorNuevo viajan ya serializados a JSON (lo hace
 * el caller — el dominio no conoce Jackson).
 */
public record AuditEntry(
    UUID empresaId,
    UUID sucursalId,
    UUID usuarioId,
    String nombreUsuario,
    String direccionIp,
    String agenteUsuario,
    String tipoEntidad,
    String entidadId,
    AccionAuditoria accion,
    String valorAnterior,
    String valorNuevo) {

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private UUID empresaId;
    private UUID sucursalId;
    private UUID usuarioId;
    private String nombreUsuario;
    private String direccionIp;
    private String agenteUsuario;
    private String tipoEntidad;
    private String entidadId;
    private AccionAuditoria accion;
    private String valorAnterior;
    private String valorNuevo;

    public Builder empresaId(UUID v) {
      this.empresaId = v;
      return this;
    }

    public Builder sucursalId(UUID v) {
      this.sucursalId = v;
      return this;
    }

    public Builder usuarioId(UUID v) {
      this.usuarioId = v;
      return this;
    }

    public Builder nombreUsuario(String v) {
      this.nombreUsuario = v;
      return this;
    }

    public Builder direccionIp(String v) {
      this.direccionIp = v;
      return this;
    }

    public Builder agenteUsuario(String v) {
      this.agenteUsuario = v;
      return this;
    }

    public Builder tipoEntidad(String v) {
      this.tipoEntidad = v;
      return this;
    }

    public Builder entidadId(String v) {
      this.entidadId = v;
      return this;
    }

    public Builder accion(AccionAuditoria v) {
      this.accion = v;
      return this;
    }

    public Builder valorAnterior(String v) {
      this.valorAnterior = v;
      return this;
    }

    public Builder valorNuevo(String v) {
      this.valorNuevo = v;
      return this;
    }

    public AuditEntry build() {
      return new AuditEntry(
          empresaId,
          sucursalId,
          usuarioId,
          nombreUsuario,
          direccionIp,
          agenteUsuario,
          tipoEntidad,
          entidadId,
          accion,
          valorAnterior,
          valorNuevo);
    }
  }
}
