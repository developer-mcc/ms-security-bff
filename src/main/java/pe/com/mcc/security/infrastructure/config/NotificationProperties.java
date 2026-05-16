package pe.com.mcc.security.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("notification")
public class NotificationProperties {

  private final Email email = new Email();

  public Email getEmail() {
    return email;
  }

  public static class Email {

    private String from = "noreply@ms-security-bff.local";

    public String getFrom() {
      return from;
    }

    public void setFrom(String from) {
      this.from = from;
    }
  }
}
