package run.halo.app.theme;

import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.result.view.View;
import org.springframework.web.server.ServerWebExchange;
import org.thymeleaf.spring6.view.reactive.ThymeleafReactiveView;
import org.thymeleaf.spring6.view.reactive.ThymeleafReactiveViewResolver;
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
            return themeResolver.getTheme(exchange.getRequest()).flatMap(theme -> {
                // calculate the engine before rendering
                setTemplateEngine(engineManager.getTemplateEngine(theme));
                return super.render(model, contentType, exchange)
                        .subscribeOn(Schedulers.boundedElastic());
            });
        }
    }

}
