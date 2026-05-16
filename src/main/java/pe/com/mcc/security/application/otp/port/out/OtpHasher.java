package pe.com.mcc.security.application.otp.port.out;

public interface OtpHasher {

  String hashear(String codigoPlano);

  boolean verificar(String codigoPlano, String hash);
}
