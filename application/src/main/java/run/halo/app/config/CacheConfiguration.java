package run.halo.app.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;
import run.halo.app.cache.PageCacheWebFilter;

@EnableCaching
@Configuration
public class CacheConfiguration {

    @Bean
    @ConditionalOnProperty(name = "halo.cache.page.disabled", havingValue = "false")
    WebFilter pageCacheWebFilter(CacheManager cacheManager) {
        return new PageCacheWebFilter(cacheManager);
    }
}
