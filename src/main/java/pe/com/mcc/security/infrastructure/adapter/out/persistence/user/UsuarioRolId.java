package pe.com.mcc.security.infrastructure.adapter.out.persistence.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class UsuarioRolId implements Serializable {

  @Column(name = "usuario_id")
  private UUID usuarioId;

  @Column(name = "rol_id", length = 30)
  private String rolId;
}
