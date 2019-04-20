package run.halo.app.event.theme;

import freemarker.template.Configuration;
import freemarker.template.TemplateModelException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import run.halo.app.handler.theme.config.support.ThemeProperty;
import run.halo.app.service.OptionService;
import run.halo.app.service.ThemeService;

import java.util.Map;

/**
 * Freemarker config aware listener.
 *
 * @author johnniang
 * @date 19-4-20
 */
@Slf4j
@Component
public class FreemarkerConfigAwareListener {

    private final OptionService optionService;

    private final Configuration configuration;

    private final ThemeService themeService;

    public FreemarkerConfigAwareListener(OptionService optionService,
                                         Configuration configuration,
                                         ThemeService themeService) {
        this.optionService = optionService;
        this.configuration = configuration;
        this.themeService = themeService;
    }

    @Async
    @EventListener
    public void onApplicationStartedEvent(ApplicationStartedEvent applicationStartedEvent) {
        try {
            ThemeProperty activatedTheme = themeService.getActivatedTheme();
            log.debug("Set shared variable theme: [{}]", activatedTheme);
            configuration.setSharedVariable("theme", activatedTheme);
        } catch (TemplateModelException e) {
            log.warn("Failed to configure freemarker", e);
        }
    }

    @Async
    @EventListener
    public void onThemeActivatedEvent(ThemeActivatedEvent themeActivatedEvent) {
        try {
            ThemeProperty activatedTheme = themeActivatedEvent.getThemeProperty();
            log.debug("Set shared variable theme: [{}]", activatedTheme);
            configuration.setSharedVariable("theme", activatedTheme);
            Map<String, String> options = optionService.listOptions();
            log.debug("Set shared variable options: [{}]", options);
            configuration.setSharedVariable("options", options);
        } catch (TemplateModelException e) {
            log.warn("Failed to configure freemarker", e);
        }
    }
}
