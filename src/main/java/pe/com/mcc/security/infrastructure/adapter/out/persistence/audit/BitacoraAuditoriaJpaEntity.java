package pe.com.mcc.security.infrastructure.adapter.out.persistence.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Tabla append-only. No se actualiza ni elimina. Cubre: - Cambios de entidad (CREATE/UPDATE/DELETE)
 * con valor_anterior/valor_nuevo como JSON. - Eventos de seguridad (LOGIN_SUCCESS, OTP_*, TOKEN_*,
 * BRANCH_SWITCHED, ...). Indexed by (empresa_id, fecha_creacion), (tipo_entidad, entidad_id),
 * (usuario_id, fecha_creacion).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(schema = "sec", name = "bitacora_auditoria")
public class BitacoraAuditoriaJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "empresa_id", updatable = false)
  private UUID empresaId;

  @Column(name = "sucursal_id", updatable = false)
  private UUID sucursalId;

  @Column(name = "usuario_id", updatable = false)
  private UUID usuarioId;

  @Column(name = "nombre_usuario", length = 50, updatable = false)
  private String nombreUsuario;

  @Column(name = "direccion_ip", length = 45, updatable = false)
  private String direccionIp;

  @Column(name = "agente_usuario", length = 500, updatable = false)
  private String agenteUsuario;

  @Column(name = "tipo_entidad", nullable = false, length = 100, updatable = false)
  private String tipoEntidad;

  @Column(name = "entidad_id", nullable = false, length = 100, updatable = false)
  private String entidadId;

  @Column(name = "accion", nullable = false, length = 40, updatable = false)
  private String accion;

  @Column(name = "valor_anterior", columnDefinition = "jsonb", updatable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private String valorAnterior;

  @Column(name = "valor_nuevo", columnDefinition = "jsonb", updatable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private String valorNuevo;

  @Column(name = "fecha_creacion", nullable = false, updatable = false)
  private LocalDateTime fechaCreacion;

  @PrePersist
  private void prePersist() {
    if (fechaCreacion == null) {
      fechaCreacion = LocalDateTime.now();
    }
  }
}
