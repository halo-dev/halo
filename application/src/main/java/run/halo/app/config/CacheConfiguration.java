package run.halo.app.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;
import run.halo.app.cache.CacheWebFilter;

@EnableCaching
@Configuration
public class CacheConfiguration {

    @Bean
    WebFilter cacheWebFilter(CacheManager cacheManager) {
        return new CacheWebFilter(cacheManager);
    }
}
