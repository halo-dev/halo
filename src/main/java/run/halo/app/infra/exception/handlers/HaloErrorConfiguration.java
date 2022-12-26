package run.halo.app.infra.exception.handlers;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

/**
 * Configuration to render errors via a WebFlux
 * {@link org.springframework.web.server.WebExceptionHandler}.
 * <br/>
 * <br/>
 * See
 * {@link org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration}
 * for more.
 *
 * @author guqing
 * @author johnniang
 * @since 2.1.0
 */
@Configuration
public class HaloErrorConfiguration {

    /**
     * This bean will replace ErrorWebExceptionHandler defined at
     * {@link ErrorWebFluxAutoConfiguration#errorWebExceptionHandler}.
     */
    @Bean
    @Order(-1)
    ErrorWebExceptionHandler errorWebExceptionHandler(ErrorAttributes errorAttributes,
        WebProperties webProperties,
        ObjectProvider<ViewResolver> viewResolvers,
        ServerCodecConfigurer serverCodecConfigurer,
        ApplicationContext applicationContext,
        ServerProperties serverProperties) {
        var exceptionHandler = new HaloErrorWebExceptionHandler(
            errorAttributes,
            webProperties.getResources(),
            serverProperties.getError(),
            applicationContext);
        exceptionHandler.setViewResolvers(viewResolvers.orderedStream().toList());
        exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
        exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
        return exceptionHandler;
    }

    /**
     * This bean will replace ErrorAttributes defined at
     * {@link ErrorWebFluxAutoConfiguration#errorAttributes}.
     */
    @Bean
    ErrorAttributes errorAttributes(MessageSource messageSource) {
        return new ProblemDetailErrorAttributes(messageSource);
    }
}
