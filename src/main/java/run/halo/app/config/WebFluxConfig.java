package run.halo.app.config;

import static org.springframework.util.ResourceUtils.FILE_URL_PREFIX;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolutionResultHandler;
import org.springframework.web.reactive.result.view.ViewResolver;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.core.extension.endpoint.CustomEndpointsBuilder;
import run.halo.app.infra.properties.HaloProperties;

@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    private final ObjectMapper objectMapper;

    private final HaloProperties haloProp;

    public WebFluxConfig(ObjectMapper objectMapper, HaloProperties haloProp) {
        this.objectMapper = objectMapper;
        this.haloProp = haloProp;
    }

    @Bean
    ServerResponse.Context context(CodecConfigurer codec,
        ViewResolutionResultHandler resultHandler) {
        return new ServerResponse.Context() {
            @Override
            @NonNull
            public List<HttpMessageWriter<?>> messageWriters() {
                return codec.getWriters();
            }

            @Override
            @NonNull
            public List<ViewResolver> viewResolvers() {
                return resultHandler.getViewResolvers();
            }
        };
    }

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        // we need to customize the Jackson2Json[Decoder][Encoder] here to serialize and
        // deserialize special types, e.g.: Instant, LocalDateTime. So we use ObjectMapper
        // created by outside.
        configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
        configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
    }

    @Bean
    RouterFunction<ServerResponse> customEndpoints(ApplicationContext context) {
        var builder = new CustomEndpointsBuilder();
        context.getBeansOfType(CustomEndpoint.class).values()
            .forEach(customEndpoint -> builder.add(customEndpoint.endpoint()));
        return builder.build();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        var attachmentsRoot = haloProp.getWorkDir().resolve("attachments");
        registry.addResourceHandler("/upload/**")
            .addResourceLocations(FILE_URL_PREFIX + attachmentsRoot + "/");
    }
}
