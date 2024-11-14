package run.halo.app.config;

import static org.springframework.util.ResourceUtils.FILE_URL_PREFIX;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.method;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static run.halo.app.infra.utils.FileUtils.checkDirectoryTraversal;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Objects;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.config.ResourceHandlerRegistration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.resource.EncodedResourceResolver;
import org.springframework.web.reactive.resource.PathResourceResolver;
import org.springframework.web.reactive.result.view.ViewResolutionResultHandler;
import org.springframework.web.reactive.result.view.ViewResolver;
import reactor.core.publisher.Mono;
import run.halo.app.console.ProxyFilter;
import run.halo.app.console.WebSocketRequestPredicate;
import run.halo.app.core.endpoint.WebSocketHandlerMapping;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.core.extension.endpoint.CustomEndpointsBuilder;
import run.halo.app.infra.properties.AttachmentProperties;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.webfilter.AdditionalWebFilterChainProxy;

// webflux 是一个新的响应式，异步编程框架。区别于spring mvc
// webflux 不仅仅是一个响应式编程框架，同时也是一个完整的http服务器，可以通过内置的netty运行。
@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    private final ObjectMapper objectMapper;

    private final HaloProperties haloProp;

    private final WebProperties.Resources resourceProperties;

    private final ApplicationContext applicationContext;

    public WebFluxConfig(ObjectMapper objectMapper,
        HaloProperties haloProp,
        WebProperties webProperties,
        ApplicationContext applicationContext) {
        this.objectMapper = objectMapper;
        this.haloProp = haloProp;
        this.resourceProperties = webProperties.getResources();
        this.applicationContext = applicationContext;
    }

    // @Bean 和 @Component 类似，都是定义Spring Bean，@Bean 注解在带有返回值的方法上，@Component 注解在类上
    // @Bean 注解的函数只有被调用后才会在spring中注册
    // @Component 注解会被自动扫描，实例化，并且注册
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

    // 自定义路由
    // 这里定义的halo后端的路由，类似于 flask 的blueprint
    @Bean
    RouterFunction<ServerResponse> customEndpoints(ApplicationContext context) {
        var builder = new CustomEndpointsBuilder();
        context.getBeansOfType(CustomEndpoint.class).values().forEach(builder::add);
        return builder.build();
    }

    @Bean
    public WebSocketHandlerMapping webSocketHandlerMapping() {
        WebSocketHandlerMapping handlerMapping = new WebSocketHandlerMapping();
        handlerMapping.setOrder(-2);
        return handlerMapping;
    }

    // 前端路由
    // RouterFunction 来定义对特定请求路径的匹配条件（Predicate），判断请求是否符合重定向的条件。
    // 包含 console/** 但是不包含 /console/assets/** （这个路径后面会处理）
    // 如果符合重定向条件，就会重定向到前端应用 */index.html 前端路由框架会来处理后续路径。
    @Bean
    RouterFunction<ServerResponse> consoleIndexRedirection() {
        var consolePredicate = method(HttpMethod.GET)
            .and(path("/console/**").and(path("/console/assets/**").negate()))
            .and(accept(MediaType.TEXT_HTML))
            .and(new WebSocketRequestPredicate().negate());
        return route(consolePredicate,
            request -> this.serveIndex(haloProp.getConsole().getLocation() + "index.html"));
    }

    // 前端路由
    // 同上
    @Bean
    RouterFunction<ServerResponse> ucIndexRedirect() {
        var consolePredicate = method(HttpMethod.GET)
            .and(path("/uc/**").and(path("/uc/assets/**").negate()))
            .and(accept(MediaType.TEXT_HTML))
            .and(new WebSocketRequestPredicate().negate());
        return route(consolePredicate,
            request -> this.serveIndex(haloProp.getUc().getLocation() + "index.html"));
    }

    private Mono<ServerResponse> serveIndex(String indexLocation) {
        var indexResource = applicationContext.getResource(indexLocation);
        try {
            return ServerResponse.ok()
                .cacheControl(CacheControl.noStore())
                .body(BodyInserters.fromResource(indexResource));
        } catch (Throwable e) {
            return Mono.error(e);
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        var attachmentsRoot = haloProp.getWorkDir().resolve("attachments");
        var cacheControl = resourceProperties.getCache()
            .getCachecontrol()
            .toHttpCacheControl();
        if (cacheControl == null) {
            cacheControl = CacheControl.empty();
        }
        final var useLastModified = resourceProperties.getCache().isUseLastModified();

        // Mandatory resource mapping
        var uploadRegistration = registry.addResourceHandler("/upload/**")
            .addResourceLocations(FILE_URL_PREFIX + attachmentsRoot.resolve("upload") + "/")
            .setUseLastModified(useLastModified)
            .setCacheControl(cacheControl);

        // For console assets
        registry.addResourceHandler("/console/assets/**")
            .addResourceLocations(haloProp.getConsole().getLocation() + "assets/")
            .setCacheControl(cacheControl)
            .setUseLastModified(useLastModified)
            .resourceChain(true)
            .addResolver(new EncodedResourceResolver())
            .addResolver(new PathResourceResolver());

        // For uc assets
        registry.addResourceHandler("/uc/assets/**")
            .addResourceLocations(haloProp.getUc().getLocation() + "assets/")
            .setCacheControl(cacheControl)
            .setUseLastModified(useLastModified)
            .resourceChain(true)
            .addResolver(new EncodedResourceResolver())
            .addResolver(new PathResourceResolver());

        // Additional resource mappings
        var staticResources = haloProp.getAttachment().getResourceMappings();
        for (AttachmentProperties.ResourceMapping staticResource : staticResources) {
            ResourceHandlerRegistration registration;
            if (Objects.equals(staticResource.getPathPattern(), "/upload/**")) {
                registration = uploadRegistration;
            } else {
                registration = registry.addResourceHandler(staticResource.getPathPattern())
                    .setCacheControl(cacheControl)
                    .setUseLastModified(useLastModified);
            }
            for (String location : staticResource.getLocations()) {
                var path = attachmentsRoot.resolve(location);
                checkDirectoryTraversal(attachmentsRoot, path);
                registration.addResourceLocations(FILE_URL_PREFIX + path + "/");
            }
        }
        // 对于其他 url（包括主页）的访问
        // 访问的是 .hola/static 这个路径下的文件以及  resourceProperties.getStaticLocations()
        // 这个路径下面的文件
        var haloStaticPath = haloProp.getWorkDir().resolve("static");
        registry.addResourceHandler("/**")
            .addResourceLocations(FILE_URL_PREFIX + haloStaticPath + "/")
            .addResourceLocations(resourceProperties.getStaticLocations())
            .setCacheControl(cacheControl)
            .setUseLastModified(useLastModified)
            .resourceChain(true)
            .addResolver(new EncodedResourceResolver())
            .addResolver(new PathResourceResolver());
    }


    @ConditionalOnProperty(name = "halo.console.proxy.enabled", havingValue = "true")
    @Bean
    ProxyFilter consoleProxyFilter() {
        return new ProxyFilter("/console/**", haloProp.getConsole().getProxy());
    }


    @ConditionalOnProperty(name = "halo.uc.proxy.enabled", havingValue = "true")
    @Bean
    ProxyFilter ucProxyFilter() {
        return new ProxyFilter("/uc/**", haloProp.getUc().getProxy());
    }

    /**
     * Create a WebFilterChainProxy for all AdditionalWebFilters.
     *
     * <p>The reason why the order is -101 is that the current
     * AdditionalWebFilterChainProxy should be executed before WebFilterChainProxy
     * and the order of WebFilterChainProxy is -100.
     *
     * <p>See {@code org.springframework.security.config.annotation.web.reactive
     * .WebFluxSecurityConfiguration#WEB_FILTER_CHAIN_FILTER_ORDER} for more
     *
     * @param extensionGetter extension getter.
     * @return additional web filter chain proxy.
     */
    @Bean
    @Order(-101)
    AdditionalWebFilterChainProxy additionalWebFilterChainProxy(ExtensionGetter extensionGetter) {
        return new AdditionalWebFilterChainProxy(extensionGetter);
    }

}
