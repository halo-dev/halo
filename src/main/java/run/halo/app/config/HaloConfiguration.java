package run.halo.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.client.RestTemplate;
import run.halo.app.cache.InMemoryCacheStore;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.filter.CorsFilter;
import run.halo.app.filter.LogFilter;
import run.halo.app.security.filter.AdminAuthenticationFilter;
import run.halo.app.security.filter.ApiAuthenticationFilter;
import run.halo.app.security.handler.AdminAuthenticationFailureHandler;
import run.halo.app.security.handler.DefaultAuthenticationFailureHandler;
import run.halo.app.service.UserService;
import run.halo.app.utils.HttpClientUtils;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * Halo configuration.
 *
 * @author johnniang
 */
@Configuration
@EnableConfigurationProperties(HaloProperties.class)
public class HaloConfiguration {

    private final static int TIMEOUT = 5000;

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        builder.failOnEmptyBeans(false);
        return builder.build();
    }

    @Bean
    public RestTemplate httpsRestTemplate(RestTemplateBuilder builder) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        RestTemplate httpsRestTemplate = builder.build();
        httpsRestTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(HttpClientUtils.createHttpsClient(TIMEOUT)));
        return httpsRestTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public StringCacheStore stringCacheStore() {
        return new InMemoryCacheStore();
    }

    /**
     * Creates a CorsFilter.
     *
     * @return Cors filter registration bean
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        FilterRegistrationBean<CorsFilter> corsFilter = new FilterRegistrationBean<>();

        corsFilter.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        corsFilter.setFilter(new CorsFilter());
        corsFilter.addUrlPatterns("/api/*");

        return corsFilter;
    }

    /**
     * Creates a LogFilter.
     *
     * @return Log filter registration bean
     */
    @Bean
    public FilterRegistrationBean<LogFilter> logFilter() {
        FilterRegistrationBean<LogFilter> logFilter = new FilterRegistrationBean<>();

        logFilter.setOrder(Ordered.HIGHEST_PRECEDENCE + 9);
        logFilter.setFilter(new LogFilter());
        logFilter.addUrlPatterns("/*");

        return logFilter;
    }

    @Bean
    public FilterRegistrationBean<ApiAuthenticationFilter> apiAuthenticationFilter(HaloProperties haloProperties, ObjectMapper objectMapper) {
        ApiAuthenticationFilter apiFilter = new ApiAuthenticationFilter(haloProperties);

        DefaultAuthenticationFailureHandler failureHandler = new DefaultAuthenticationFailureHandler();
        failureHandler.setProductionEnv(haloProperties.isProductionEnv());
        failureHandler.setObjectMapper(objectMapper);

        // Set failure handler
        apiFilter.setFailureHandler(failureHandler);
        apiFilter.addExcludeUrlPatterns("/api/admin/*");

        FilterRegistrationBean<ApiAuthenticationFilter> authenticationFilter = new FilterRegistrationBean<>();
        authenticationFilter.setFilter(apiFilter);
        authenticationFilter.addUrlPatterns("/api/*");
        authenticationFilter.setOrder(0);
        return authenticationFilter;
    }

    @Bean
    public FilterRegistrationBean<AdminAuthenticationFilter> adminAuthenticationFilter(StringCacheStore cacheStore,
                                                                                       UserService userService,
                                                                                       HaloProperties haloProperties,
                                                                                       ObjectMapper objectMapper) {
        AdminAuthenticationFilter adminAuthenticationFilter = new AdminAuthenticationFilter(cacheStore, userService, haloProperties);

        AdminAuthenticationFailureHandler failureHandler = new AdminAuthenticationFailureHandler();
        failureHandler.setProductionEnv(haloProperties.isProductionEnv());
        failureHandler.setObjectMapper(objectMapper);

        // Config the admin filter
        adminAuthenticationFilter.addExcludeUrlPatterns("/api/admin/login");
        adminAuthenticationFilter.addTryAuthUrlMethodPattern("/api/admin/comments", HttpMethod.POST.name());
        adminAuthenticationFilter.addTryAuthUrlMethodPattern("/api/comments", HttpMethod.POST.name());
        adminAuthenticationFilter.setFailureHandler(
                failureHandler);

        FilterRegistrationBean<AdminAuthenticationFilter> authenticationFilter = new FilterRegistrationBean<>();
        authenticationFilter.setFilter(adminAuthenticationFilter);
        authenticationFilter.addUrlPatterns("/api/admin/*", "/api/comments");
        authenticationFilter.setOrder(1);
        return authenticationFilter;
    }
}
