package pe.com.mcc.security.infrastructure.adapter.out.persistence.user;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.data.domain.Persistable;
import pe.com.mcc.security.infrastructure.adapter.out.persistence.shared.BaseAuditableJpaEntity;

/**
 * @FilterDef "tenantFilter" se declara aquí (entidad concreta, Hibernate la procesa). Cualquier
 * otra @Entity puede aplicar @Filter(name = "tenantFilter", ...) sin redeclarar el FilterDef.
 *
 * <p>El filtro lo activa el TenantFilterAspect leyendo el TenantContext. Cuando empresa_id IS NULL
 * (SUPER_ADMIN), el aspect no activa el filtro y este usuario ve todas las empresas.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(schema = "sec", name = "usuarios")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = UUID.class))
@Filter(name = "tenantFilter", condition = "empresa_id = :tenantId")
public class UsuarioJpaEntity extends BaseAuditableJpaEntity implements Persistable<UUID> {

  @Id
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Transient private boolean isNew = true;

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public boolean isNew() {
    return isNew;
  }

  @PostPersist
  @PostLoad
  void markNotNew() {
    isNew = false;
  }

  // NULL para SUPER_ADMIN (sin empresa asignada)
  @Column(name = "empresa_id")
  private UUID empresaId;

  @Column(name = "nombre_usuario", nullable = false, unique = true, length = 50)
  private String nombreUsuario;

  @Column(name = "correo", nullable = false, unique = true, length = 150)
  private String correo;

  @Column(name = "contrasena_hash", nullable = false, length = 255)
  private String contrasenaHash;

  @Column(name = "nombres", nullable = false, length = 100)
  private String nombres;

  @Column(name = "apellidos", nullable = false, length = 100)
  private String apellidos;

  @Column(name = "dni", length = 15)
  private String dni;

  @Column(name = "telefono", length = 20)
  private String telefono;

  @Column(name = "canal_otp_preferido", nullable = false, length = 20)
  private String canalOtpPreferido;

  @Column(name = "mfa_habilitado", nullable = false)
  private boolean mfaHabilitado = true;

  @Column(name = "estado", nullable = false, length = 30)
  private String estado;

  @Column(name = "intentos_fallidos", nullable = false)
  private int intentosFallidos;

  @Column(name = "bloqueado_hasta")
  private LocalDateTime bloqueadoHasta;

  @Column(name = "ultimo_acceso")
  private LocalDateTime ultimoAcceso;

  @Column(name = "contrasena_cambiada_en", nullable = false)
  private LocalDateTime contrasenaCambiadaEn;

  @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<UsuarioRolJpaEntity> roles = new ArrayList<>();

  @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<UsuarioSucursalJpaEntity> sucursales = new ArrayList<>();

  @PrePersist
  private void prePersist() {
    if (estado == null) {
      estado = "ACTIVO";
    }
    if (canalOtpPreferido == null) {
      canalOtpPreferido = "EMAIL";
    }
    if (contrasenaCambiadaEn == null) {
      contrasenaCambiadaEn = LocalDateTime.now();
    }
  }
}
