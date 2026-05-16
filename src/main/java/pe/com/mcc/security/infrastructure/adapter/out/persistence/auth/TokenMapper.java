package pe.com.mcc.security.infrastructure.adapter.out.persistence.auth;

import java.util.Optional;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.domain.token.model.MotivoRevocacion;
import pe.com.mcc.security.domain.token.model.TipoToken;
import pe.com.mcc.security.domain.token.model.Token;

@Component
public class TokenMapper {

  public Token toDomain(TokenJpaEntity e) {
    return new Token(
        e.getJti(),
        e.getSesionId(),
        e.getUsuarioId(),
        e.getEmpresaId(),
        e.getSucursalId(),
        TipoToken.valueOf(e.getTipo()),
        e.getJtiPadre(),
        e.getHuellaDispositivo(),
        e.getDireccionIp(),
        e.getAgenteUsuario(),
        e.getEmitidoEn(),
        e.getExpiraEn(),
        e.isRevocado(),
        e.getRevocadoEn(),
        Optional.ofNullable(e.getMotivoRevocacion()).map(MotivoRevocacion::valueOf).orElse(null));
  }

  public TokenJpaEntity toEntity(Token d) {
    TokenJpaEntity e = new TokenJpaEntity();
    e.setJti(d.jti());
    e.setSesionId(d.sesionId());
    e.setUsuarioId(d.usuarioId());
    e.setEmpresaId(d.empresaId());
    e.setSucursalId(d.sucursalId());
    e.setTipo(d.tipo().name());
    e.setJtiPadre(d.jtiPadre());
    e.setHuellaDispositivo(d.huellaDispositivo());
    e.setDireccionIp(d.direccionIp());
    e.setAgenteUsuario(d.agenteUsuario());
    e.setEmitidoEn(d.emitidoEn());
    e.setExpiraEn(d.expiraEn());
    e.setRevocado(d.revocado());
    e.setRevocadoEn(d.revocadoEn());
    e.setMotivoRevocacion(d.motivoRevocacion() != null ? d.motivoRevocacion().name() : null);
    return e;
  }
}
