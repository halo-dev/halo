package run.halo.app.listener.freemarker;

import freemarker.template.Configuration;
import freemarker.template.TemplateModelException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import run.halo.app.event.options.OptionUpdatedEvent;
import run.halo.app.event.theme.ThemeActivatedEvent;
import run.halo.app.event.user.UserUpdatedEvent;
import run.halo.app.handler.theme.config.support.ThemeProperty;
import run.halo.app.model.support.HaloConst;
import run.halo.app.service.OptionService;
import run.halo.app.service.ThemeService;
import run.halo.app.service.ThemeSettingService;
import run.halo.app.service.UserService;

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

    private final UserService userService;

    public FreemarkerConfigAwareListener(OptionService optionService,
                                         Configuration configuration,
                                         ThemeService themeService,
                                         ThemeSettingService themeSettingService,
                                         UserService userService) {
        this.optionService = optionService;
        this.configuration = configuration;
        this.themeService = themeService;
        this.themeSettingService = themeSettingService;
        this.userService = userService;
    }

    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public void onApplicationStartedEvent(ApplicationStartedEvent applicationStartedEvent) throws TemplateModelException {
        log.debug("Received application started event");

        loadThemeConfig();
        loadOptionsConfig();
        loadUserConfig();
    }

    @EventListener
    public void onThemeActivatedEvent(ThemeActivatedEvent themeActivatedEvent) throws TemplateModelException {
        log.debug("Received theme activated event");

        loadThemeConfig();
    }

    @EventListener
    public void onUserUpdate(UserUpdatedEvent event) throws TemplateModelException {
        log.debug("Received user updated event, user id: [{}]", event.getUserId());

        loadUserConfig();
    }

    @EventListener
    public void onOptionUpdate(OptionUpdatedEvent event) throws TemplateModelException {
        log.debug("Received option updated event");

        loadOptionsConfig();
        loadThemeConfig();
    }


    private void loadUserConfig() throws TemplateModelException {
        configuration.setSharedVariable("user", userService.getCurrentUser().orElse(null));
        log.debug("Loaded user");
    }

    private void loadOptionsConfig() throws TemplateModelException {
        configuration.setSharedVariable("options", optionService.listOptions());
        configuration.setSharedVariable("context", optionService.getBlogBaseUrl());
        configuration.setSharedVariable("version", HaloConst.HALO_VERSION);
        log.debug("Loaded options");
    }

    private void loadThemeConfig() throws TemplateModelException {
        ThemeProperty activatedTheme = themeService.getActivatedTheme();
        configuration.setSharedVariable("theme", activatedTheme);
        configuration.setSharedVariable("static", optionService.getBlogBaseUrl() + "/" + activatedTheme.getFolderName());
        configuration.setSharedVariable("settings", themeSettingService.listAsMapBy(themeService.getActivatedThemeId()));
        log.debug("Loaded theme and settings");
    }
}
