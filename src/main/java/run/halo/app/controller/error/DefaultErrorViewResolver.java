package run.halo.app.controller.error;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.template.TemplateAvailabilityProviders;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus.Series;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import run.halo.app.service.ThemeService;

@Component
public class DefaultErrorViewResolver implements ErrorViewResolver {

    private static final Map<Series, String> SERIES_VIEWS;

    static {
        EnumMap<Series, String> views = new EnumMap<>(Series.class);
        views.put(Series.CLIENT_ERROR, "4xx");
        views.put(Series.SERVER_ERROR, "5xx");
        SERIES_VIEWS = Collections.unmodifiableMap(views);
    }

    private final ThemeService themeService;

    private final TemplateAvailabilityProviders templateAvailabilityProviders;

    private final ApplicationContext applicationContext;

    public DefaultErrorViewResolver(ThemeService themeService,
        ApplicationContext applicationContext) {
        this.themeService = themeService;
        this.applicationContext = applicationContext;
        this.templateAvailabilityProviders = new TemplateAvailabilityProviders(applicationContext);
    }

    @Override
    public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status,
        Map<String, Object> model) {
        // for compatibility
        var errorModel = new HashMap<>(model);

        // resolve with status code. eg: 400.ftl
        var modelAndView = resolve(String.valueOf(status.value()), errorModel);

        // resolve with status series. eg: 4xx.ftl
        if (modelAndView == null && SERIES_VIEWS.containsKey(status.series())) {
            modelAndView = resolve(SERIES_VIEWS.get(status.series()), errorModel);
        }

        // resolve error template. eg: error.ftl
        if (modelAndView == null) {
            modelAndView = resolve("error", errorModel);
        }

        if (modelAndView == null) {
            // resolve common error template
            modelAndView = new ModelAndView("common/error/error", errorModel);
        }

        return modelAndView;
    }

    private ModelAndView resolve(String viewName, Map<String, Object> model) {
        var errorViewName = this.themeService.render(viewName);
        var provider =
            this.templateAvailabilityProviders.getProvider(errorViewName, this.applicationContext);
        if (provider != null) {
            return new ModelAndView(errorViewName, model);
        }
        return null;
    }

}
