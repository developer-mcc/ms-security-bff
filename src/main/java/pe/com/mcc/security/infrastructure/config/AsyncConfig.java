package pe.com.mcc.security.infrastructure.config;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Executor dedicado a eventos de seguridad (login/logout/token revoked/...). Aislado del thread
 * pool web para que un pico de notificaciones no bloquee requests.
 *
 * <p>Los listeners se registran como @Async("securityEventsExecutor") para usarlo. Si nombre lo
 * omites, Spring usa el bean "taskExecutor" por defecto.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

  public static final String SECURITY_EVENTS_EXECUTOR = "securityEventsExecutor";

  @Bean(name = SECURITY_EVENTS_EXECUTOR)
  public TaskExecutor securityEventsExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(500);
    executor.setKeepAliveSeconds(60);
    executor.setThreadNamePrefix("sec-events-");
    executor.setTaskDecorator(mdcPropagatingDecorator());
    // Si la cola se llena, el caller (hilo del listener) ejecuta la tarea —
    // preferible a perder eventos de auditoría.
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(30);
    executor.initialize();
    return executor;
  }

  private static TaskDecorator mdcPropagatingDecorator() {
    return task -> {
      Map<String, String> context = MDC.getCopyOfContextMap();
      return () -> {
        try {
          if (context != null) {
            MDC.setContextMap(context);
          }
          task.run();
        } finally {
          MDC.clear();
        }
      };
    };
  }
}
