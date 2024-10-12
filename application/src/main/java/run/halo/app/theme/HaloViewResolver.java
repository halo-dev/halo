package run.halo.app.theme;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.attoparser.ParseException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.ErrorResponse;
import org.springframework.web.reactive.result.view.View;
import org.springframework.web.server.ServerWebExchange;
import org.thymeleaf.exceptions.TemplateInputException;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.spring6.view.reactive.ThymeleafReactiveView;
import org.thymeleaf.spring6.view.reactive.ThymeleafReactiveViewResolver;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.theme.finders.FinderRegistry;
import run.halo.app.theme.router.ModelConst;

@Component("thymeleafReactiveViewResolver")
public class HaloViewResolver extends ThymeleafReactiveViewResolver implements InitializingBean {

    private final FinderRegistry finderRegistry;

    private final ThymeleafProperties thymeleafProperties;

    public HaloViewResolver(FinderRegistry finderRegistry,
        ThymeleafProperties thymeleafProperties) {
        this.finderRegistry = finderRegistry;
        this.thymeleafProperties = thymeleafProperties;
    }

    @Override
    protected Mono<View> loadView(String viewName, Locale locale) {
        return super.loadView(viewName, locale)
            .cast(HaloView.class)
            .map(view -> {
                // populate finders to view static variables
                finderRegistry.getFinders().forEach(view::addStaticVariable);
                return view;
            });
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setViewClass(HaloView.class);
        var map = PropertyMapper.get();
        map.from(thymeleafProperties::getEncoding)
            .whenNonNull()
            .to(this::setDefaultCharset);
        map.from(thymeleafProperties::getExcludedViewNames)
            .whenNonNull()
            .to(this::setExcludedViewNames);
        map.from(thymeleafProperties::getViewNames)
            .whenNonNull()
            .to(this::setViewNames);

        var reactive = thymeleafProperties.getReactive();
        map.from(reactive::getMediaTypes)
            .whenNonNull()
            .to(this::setSupportedMediaTypes);
        map.from(reactive::getFullModeViewNames)
            .whenNonNull()
            .to(this::setFullModeViewNames);
        map.from(reactive::getChunkedModeViewNames)
            .whenNonNull()
            .to(this::setChunkedModeViewNames);
        map.from(reactive::getMaxChunkSize)
            .asInt(DataSize::toBytes)
            .when(size -> size > 0)
            .to(this::setResponseMaxChunkSizeBytes);
        setOrder(Ordered.LOWEST_PRECEDENCE - 5);
    }

    public static class HaloView extends ThymeleafReactiveView {

        @Autowired
        private TemplateEngineManager engineManager;

        @Autowired
        private ThemeResolver themeResolver;

        @Override
        public Mono<Void> render(Map<String, ?> model, MediaType contentType,
            ServerWebExchange exchange) {
            return themeResolver.getTheme(exchange).flatMap(theme -> {
                // calculate the engine before rendering
                setTemplateEngine(engineManager.getTemplateEngine(theme));
                var noCache = (Boolean) exchange.getAttributes()
                    .getOrDefault(ModelConst.NO_CACHE, false);
                exchange.getAttributes()
                    .put(ModelConst.POWERED_BY_HALO_TEMPLATE_ENGINE, !noCache);
                return super.render(model, contentType, exchange)
                    .onErrorMap(TemplateProcessingException.class::isInstance, tee -> {
                        if (tee instanceof TemplateInputException) {
                            // map the error response exception while fragment not found
                            return Optional.of(tee)
                                .map(Throwable::getCause)
                                .filter(ParseException.class::isInstance)
                                .map(Throwable::getCause)
                                .filter(TemplateProcessingException.class::isInstance)
                                .map(Throwable::getCause)
                                .filter(ErrorResponse.class::isInstance)
                                .orElse(tee);
                        }
                        // map the error response exception while template not found
                        return Optional.of(tee)
                            .map(Throwable::getCause)
                            .filter(ErrorResponse.class::isInstance)
                            .orElse(tee);
                    });

            });
        }

        @Override
        @NonNull
        protected Mono<Map<String, Object>> getModelAttributes(Map<String, ?> model,
            @NonNull ServerWebExchange exchange) {
            Mono<Map<String, Object>> contextBasedStaticVariables =
                getContextBasedStaticVariables(exchange);
            Mono<Map<String, Object>> modelAttributes = super.getModelAttributes(model, exchange);
            return Flux.merge(modelAttributes, contextBasedStaticVariables)
                .collectList()
                .map(modelMapList -> {
                    Map<String, Object> result = new HashMap<>();
                    modelMapList.forEach(result::putAll);
                    return result;
                });
        }

        @NonNull
        private Mono<Map<String, Object>> getContextBasedStaticVariables(
            ServerWebExchange exchange) {
            ApplicationContext applicationContext = obtainApplicationContext();

            return Mono.just(new HashMap<String, Object>())
                .flatMap(staticVariables -> {
                    List<Mono<Map<String, Object>>> monoList = applicationContext.getBeansOfType(
                            ViewContextBasedVariablesAcquirer.class)
                        .values()
                        .stream()
                        .map(acquirer -> acquirer.acquire(exchange))
                        .toList();
                    return Flux.merge(monoList)
                        .collectList()
                        .map(modelList -> {
                            Map<String, Object> mergedModel = new HashMap<>();
                            modelList.forEach(mergedModel::putAll);
                            return mergedModel;
                        })
                        .map(mergedModel -> {
                            staticVariables.putAll(mergedModel);
                            return staticVariables;
                        });
                });
        }
    }
}
