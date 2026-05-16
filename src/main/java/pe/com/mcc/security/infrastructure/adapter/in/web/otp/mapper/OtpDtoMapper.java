package pe.com.mcc.security.infrastructure.adapter.in.web.otp.mapper;

import java.util.UUID;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.otp.port.in.SolicitarOtpCommand;
import pe.com.mcc.security.application.otp.port.in.VerificarOtpCommand;
import pe.com.mcc.security.infrastructure.adapter.in.web.otp.dto.SolicitarOtpRequest;
import pe.com.mcc.security.infrastructure.adapter.in.web.otp.dto.VerificarOtpRequest;

@Component
public class OtpDtoMapper {

  public SolicitarOtpCommand toCommand(
      UUID usuarioId, SolicitarOtpRequest request, String direccionIp) {
    return new SolicitarOtpCommand(usuarioId, request.canal(), request.proposito(), direccionIp);
  }

  public VerificarOtpCommand toCommand(
      UUID usuarioId, VerificarOtpRequest request, String direccionIp) {
    return new VerificarOtpCommand(usuarioId, request.codigo(), request.proposito(), direccionIp);
  }
}
