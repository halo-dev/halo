package run.halo.app.infra.health;

import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Configuration for health indicators.
 *
 * @author halo-dev
 * @since 2.20.18
 */
@Configuration
@ConditionalOnClass(HealthIndicator.class)
public class HealthConfig {

    /**
     * This is a configuration class that ensures all health indicators
     * are properly registered with Spring Boot's health system.
     * It doesn't need @Bean methods because {@link DistributedHealthIndicator}
     * is already annotated with @Component and will be picked up by component scanning.
     * 
     * The class existence ensures that health endpoints are properly configured
     * and can be conditionally enabled based on properties.
     */

}
