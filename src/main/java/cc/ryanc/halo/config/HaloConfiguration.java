package cc.ryanc.halo.config;

import cc.ryanc.halo.config.properties.HaloProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Halo configuration.
 *
 * @author johnniang
 */
@Configuration
@EnableConfigurationProperties(HaloProperties.class)
public class HaloConfiguration {

}
