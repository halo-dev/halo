package run.halo.app.theme;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.thymeleaf.spring6.view.reactive.ThymeleafReactiveView;
import org.thymeleaf.spring6.view.reactive.ThymeleafReactiveViewResolver;
import reactor.core.publisher.Mono;

@Component("thymeleafReactiveViewResolver")
public class HaloViewResolver extends ThymeleafReactiveViewResolver {

    public HaloViewResolver() {
        setViewClass(HaloView.class);
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
                return super.render(model, contentType, exchange);
            });
        }
    }

}
