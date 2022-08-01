package run.halo.app.theme.newplan;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.thymeleaf.spring6.ISpringWebFluxTemplateEngine;
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
            // calculate the engine before rendering
            var theme = themeResolver.getTheme(exchange.getRequest());
            var templateEngine = engineManager.getTemplateEngine(theme);
            setTemplateEngine(templateEngine);

            return super.render(model, contentType, exchange);
        }

        @Override
        protected ISpringWebFluxTemplateEngine getTemplateEngine() {
            return super.getTemplateEngine();
        }
    }

}
