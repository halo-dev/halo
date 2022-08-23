package run.halo.app.theme;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.thymeleaf.spring6.view.reactive.ThymeleafReactiveView;
import org.thymeleaf.spring6.view.reactive.ThymeleafReactiveViewResolver;
import reactor.core.publisher.Mono;
import run.halo.app.theme.finders.Finder;

@Component("thymeleafReactiveViewResolver")
public class HaloViewResolver extends ThymeleafReactiveViewResolver implements InitializingBean {

    public HaloViewResolver() {
        setViewClass(HaloView.class);
    }

    private void populateStaticVariables() {
        Map<String, Object> model = new HashMap<>();
        getApplicationContext().getBeansWithAnnotation(Finder.class)
            .forEach((key, value) -> {
                Finder annotation = value.getClass().getAnnotation(Finder.class);
                String finderName = annotation.value();
                if (StringUtils.isBlank(finderName)) {
                    finderName = value.getClass().getSimpleName();
                }
                if (model.containsKey(finderName)) {
                    throw new IllegalStateException("Duplicate finder name: " + finderName);
                }
                model.put(finderName, value);
            });
        setStaticVariables(model);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        populateStaticVariables();
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
