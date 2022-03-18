package run.halo.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import run.halo.app.cache.AbstractStringCacheStore;
import run.halo.app.cache.InMemoryCacheStore;
import run.halo.app.cache.LevelCacheStore;
import run.halo.app.cache.RedisCacheStore;
import run.halo.app.config.attributeconverter.AttributeConverterAutoGenerateConfiguration;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.repository.base.BaseRepositoryImpl;
import run.halo.app.utils.HttpClientUtils;

/**
 * Halo configuration.
 *
 * @author johnniang
 */
@Slf4j
@EnableAsync
@EnableCaching
@EnableScheduling
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(HaloProperties.class)
@EnableJpaRepositories(basePackages = "run.halo.app.repository", repositoryBaseClass =
    BaseRepositoryImpl.class)
@Import(AttributeConverterAutoGenerateConfiguration.class)
public class HaloConfiguration {

    private final HaloProperties haloProperties;

    private final StringRedisTemplate stringRedisTemplate;

    public HaloConfiguration(HaloProperties haloProperties,
        StringRedisTemplate stringRedisTemplate) {
        this.haloProperties = haloProperties;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Bean
    ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        builder.failOnEmptyBeans(false);
        return builder.build();
    }

    @Bean
    RestTemplate httpsRestTemplate(RestTemplateBuilder builder)
        throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        RestTemplate httpsRestTemplate = builder.build();
        httpsRestTemplate.setRequestFactory(
            new HttpComponentsClientHttpRequestFactory(HttpClientUtils.createHttpsClient(
                (int) haloProperties.getDownloadTimeout().toMillis())));
        return httpsRestTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    AbstractStringCacheStore stringCacheStore() {
        AbstractStringCacheStore stringCacheStore;
        switch (haloProperties.getCache()) {
            case "level":
                stringCacheStore = new LevelCacheStore(this.haloProperties);
                break;
            case "redis":
                stringCacheStore = new RedisCacheStore(stringRedisTemplate);
                break;
            case "memory":
            default:
                stringCacheStore = new InMemoryCacheStore();
                break;
        }
        log.info("Halo cache store load impl : [{}]", stringCacheStore.getClass());
        return stringCacheStore;
    }
}
