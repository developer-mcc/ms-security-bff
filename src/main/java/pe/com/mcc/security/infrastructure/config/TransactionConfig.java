package pe.com.mcc.security.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Reduce el order del transactional advice a 0 (más prioritario / más OUTER en la cadena de
 * proxies). Esto permite que el TenantFilterAspect (Order = 1) se ejecute DENTRO de la transacción,
 * donde ya hay una Session Hibernate abierta para llamar enableFilter(...).
 *
 * <p>Por defecto, @Transactional usa Ordered.LOWEST_PRECEDENCE = Integer.MAX_VALUE (innermost), lo
 * que impediría que cualquier otro aspect corra después.
 */
@Configuration
@EnableTransactionManagement(order = 0)
public class TransactionConfig {}
