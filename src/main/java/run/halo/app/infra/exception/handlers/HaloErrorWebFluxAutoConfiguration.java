package run.halo.app.infra.exception.handlers;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

/**
 * Global exception handler auto configuration.
 *
 * @author guqing
 * @see GlobalErrorWebExceptionHandler
 * @see
 * <a href="https://docs.spring.io/spring-framework/docs/6.0.0-SNAPSHOT/reference/html/web-reactive.html#webflux-ann-rest-exceptions-render>webflux-ann-rest-exceptions-render</a>
 * @since 2.0.0
 */
@Configuration
@EnableConfigurationProperties({ServerProperties.class, WebProperties.class})
public class HaloErrorWebFluxAutoConfiguration {
    private final ServerProperties serverProperties;

    public HaloErrorWebFluxAutoConfiguration(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    @Bean
    @Order(-2)
    public ErrorWebExceptionHandler errorWebExceptionHandler(ErrorAttributes errorAttributes,
        WebProperties webProperties, ObjectProvider<ViewResolver> viewResolvers,
        ServerCodecConfigurer serverCodecConfigurer, ApplicationContext applicationContext) {
        GlobalErrorWebExceptionHandler exceptionHandler =
            new GlobalErrorWebExceptionHandler(errorAttributes,
                webProperties.getResources(), this.serverProperties.getError(), applicationContext);
        exceptionHandler.setViewResolvers(viewResolvers.orderedStream().toList());
        exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
        exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
        return exceptionHandler;
    }

}
