package run.halo.app.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.PathMatchConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolutionResultHandler;
import org.springframework.web.reactive.result.view.ViewResolver;
import run.halo.app.Application;

@Configuration
@EnableWebFlux
public class WebFluxConfig implements WebFluxConfigurer {

    @Override
    public void configurePathMatching(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api/v1",
            c -> HandlerTypePredicate.forAnnotation(RestController.class)
                .and(HandlerTypePredicate.forBasePackage(Application.class.getPackageName()))
                .test(c)
        );
    }

    @Bean
    ServerResponse.Context context(CodecConfigurer codec,
        ViewResolutionResultHandler resultHandler) {
        return new ServerResponse.Context() {
            @Override
            public List<HttpMessageWriter<?>> messageWriters() {
                return codec.getWriters();
            }

            @Override
            public List<ViewResolver> viewResolvers() {
                return resultHandler.getViewResolvers();
            }
        };
    }
}
