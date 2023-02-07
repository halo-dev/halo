package run.halo.app.theme;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.result.view.View;
import org.springframework.web.server.ServerWebExchange;
import org.thymeleaf.spring6.view.reactive.ThymeleafReactiveView;
import org.thymeleaf.spring6.view.reactive.ThymeleafReactiveViewResolver;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.theme.finders.FinderRegistry;

@Component("thymeleafReactiveViewResolver")
public class HaloViewResolver extends ThymeleafReactiveViewResolver {

    private final FinderRegistry finderRegistry;

    public HaloViewResolver(FinderRegistry finderRegistry) {
        setViewClass(HaloView.class);
        this.finderRegistry = finderRegistry;
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
                return super.render(model, contentType, exchange)
                    .subscribeOn(Schedulers.boundedElastic());
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
