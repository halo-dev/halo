package run.halo.app.event.freemarker;

import freemarker.template.Configuration;
import freemarker.template.TemplateModelException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import run.halo.app.event.options.OptionUpdatedEvent;
import run.halo.app.event.theme.ThemeActivatedEvent;
import run.halo.app.event.user.UserUpdatedEvent;
import run.halo.app.handler.theme.config.support.ThemeProperty;
import run.halo.app.service.OptionService;
import run.halo.app.service.ThemeService;
import run.halo.app.service.ThemeSettingService;
import run.halo.app.service.UserService;

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

    private final ThemeSettingService themeSettingService;

    private final OptionService optionsService;

    private final UserService userService;

    public FreemarkerConfigAwareListener(OptionService optionService,
                                         Configuration configuration,
                                         ThemeService themeService,
                                         ThemeSettingService themeSettingService,
                                         OptionService optionsService,
                                         UserService userService) {
        this.optionService = optionService;
        this.configuration = configuration;
        this.themeService = themeService;
        this.themeSettingService = themeSettingService;
        this.optionsService = optionsService;
        this.userService = userService;
    }

    @Async
    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public void onApplicationStartedEvent(ApplicationStartedEvent applicationStartedEvent) throws TemplateModelException {
        log.debug("Received application started event");

        loadThemeConfig();
        loadOptionsConfig();
        loadUserConfig();
    }

    @Async
    @EventListener
    public void onThemeActivatedEvent(ThemeActivatedEvent themeActivatedEvent) throws TemplateModelException {
        log.debug("Received theme activated event");

        loadThemeConfig();
    }

    @Async
    @EventListener
    public void onUserUpdate(UserUpdatedEvent event) throws TemplateModelException {
        log.debug("Received user updated event, user id: [{}]", event.getUserId());

        loadUserConfig();
    }

    @Async
    @EventListener
    public void onOptionUpdate(OptionUpdatedEvent event) throws TemplateModelException {
        log.debug("Received option updated event");

        loadOptionsConfig();
    }


    private void loadUserConfig() throws TemplateModelException {
        configuration.setSharedVariable("user", userService.getCurrentUser().orElse(null));
    }

    private void loadOptionsConfig() throws TemplateModelException {
        Map<String, Object> options = optionService.listOptions();
        configuration.setSharedVariable("options", options);
    }

    private void loadThemeConfig() throws TemplateModelException {
        ThemeProperty activatedTheme = themeService.getActivatedTheme();
        configuration.setSharedVariable("theme", activatedTheme);
        configuration.setSharedVariable("settings", themeSettingService.listAsMapBy(themeService.getActivatedThemeId()));
    }
}
