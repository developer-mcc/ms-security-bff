package pe.com.mcc.security.infrastructure.adapter.out.persistence.otp;

import org.springframework.stereotype.Component;
import pe.com.mcc.security.domain.otp.model.CodigoOtp;
import pe.com.mcc.security.domain.otp.model.PropositoOtp;
import pe.com.mcc.security.domain.user.model.CanalOtp;

@Component
class CodigoOtpMapper {

  CodigoOtp toDomain(CodigoOtpJpaEntity entity) {
    return CodigoOtp.reconstituir(
        entity.getId(),
        entity.getUsuarioId(),
        entity.getCodigoHash(),
        CanalOtp.valueOf(entity.getCanal()),
        PropositoOtp.valueOf(entity.getProposito()),
        entity.getIntentos(),
        entity.getExpiraEn(),
        entity.getUsadoEn(),
        entity.getDireccionIp());
  }

  CodigoOtpJpaEntity toEntity(CodigoOtp domain) {
    CodigoOtpJpaEntity entity = new CodigoOtpJpaEntity();
    entity.setId(domain.id());
    entity.setUsuarioId(domain.usuarioId());
    entity.setCodigoHash(domain.codigoHash());
    entity.setCanal(domain.canal().name());
    entity.setProposito(domain.proposito().name());
    entity.setIntentos(domain.intentos());
    entity.setIntentosMaximos(CodigoOtp.INTENTOS_MAXIMOS);
    entity.setExpiraEn(domain.expiraEn());
    entity.setUsadoEn(domain.usadoEn());
    entity.setDireccionIp(domain.direccionIp());
    return entity;
  }
}
