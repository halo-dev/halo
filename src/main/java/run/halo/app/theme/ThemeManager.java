package run.halo.app.theme;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractCachingViewResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import run.halo.app.config.theme.ThemeConfiguration;
import run.halo.app.event.menu.MenuUpdateEvent;
import run.halo.app.event.options.OptionUpdatedEvent;
import run.halo.app.event.theme.ThemeActivatedEvent;
import run.halo.app.event.theme.ThemeUpdatedEvent;
import run.halo.app.event.user.UserUpdatedEvent;
import run.halo.app.service.MenuService;
import run.halo.app.service.OptionService;
import run.halo.app.service.ThemeService;
import run.halo.app.service.ThemeSettingService;
import run.halo.app.service.UserService;

/**
 * Mvc theme manager.
 *
 * @author Chuntung Ho
 */
@Slf4j
public class ThemeManager {
    @Resource
    private MenuService menuService;
    @Resource
    private UserService userService;
    @Resource
    private ThemeService themeService;
    @Resource
    private ThemeSettingService themeSettingService;
    @Resource
    private OptionService optionService;
    @Resource
    private ThemeConfiguration.I18nThemeSource i18nThemeSource;

    private static final String FREEMARKER = "freemarker";
    private static final String THYMELEAF = "thymeleaf";

    private Map<String, AbstractCachingViewResolver> viewResolverMap = new HashMap<>();

    public ThemeManager(ViewResolver... viewResolvers) {
        for (ViewResolver viewResolver : viewResolvers) {
            if (viewResolver instanceof ThymeleafViewResolver) {
                viewResolverMap.put(THYMELEAF, (ThymeleafViewResolver) viewResolver);
            } else if (viewResolver instanceof FreeMarkerViewResolver) {
                viewResolverMap.put(FREEMARKER, (FreeMarkerViewResolver) viewResolver);
            }
        }
    }

    public boolean templateExists(String viewName, Locale locale) {
        if (locale == null) {
            locale = optionService.getLocale();
        }
        String engine = themeService.getActivatedTheme().getEngine();
        engine = engine == null ? FREEMARKER : engine.toLowerCase(Locale.ROOT);
        AbstractCachingViewResolver viewResolver = viewResolverMap.get(engine);
        try {
            return viewResolver.resolveViewName(viewName, locale) != null;
        } catch (Exception e) {
            log.error("Failed to resolve view: {}", viewName, e);
            return false;
        }
    }

    // clear view cache when static variables change or current theme is switched.
    private void clearViewCache() {
        viewResolverMap.values().forEach(AbstractCachingViewResolver::clearCache);
    }

    // clear template cache when theme file is changed.
    private void clearTemplateCache() {
        ThymeleafViewResolver viewResolver = (ThymeleafViewResolver) viewResolverMap.get(THYMELEAF);
        ((SpringTemplateEngine) viewResolver.getTemplateEngine()).clearTemplateCache();
    }

    // clear i18n cache where i18n file (properties) is changed or default locale is changed.
    private void clearI18nCache() {
        i18nThemeSource.clearCache();
    }

    private void loadUser() {
        ThymeleafViewResolver viewResolver = (ThymeleafViewResolver) viewResolverMap.get(THYMELEAF);
        viewResolver.addStaticVariable("user", userService.getCurrentUser().orElse(null));

        // TODO for freemarker
    }

    private void loadMenus() {
        ThymeleafViewResolver viewResolver = (ThymeleafViewResolver) viewResolverMap.get(THYMELEAF);
        viewResolver.addStaticVariable("menus", menuService.listByTeam("", Sort.by("priority")));

        // TODO for freemarker
    }

    private void loadOptions() {
        ThymeleafViewResolver viewResolver = (ThymeleafViewResolver) viewResolverMap.get(THYMELEAF);
        viewResolver.addStaticVariable("options", optionService.listOptions());

        i18nThemeSource.setDefaultLocale(optionService.getLocale());
        // TODO for freemarker
    }

    private void loadThemeSettings(String themeId) {
        ThymeleafViewResolver viewResolver = (ThymeleafViewResolver) viewResolverMap.get(THYMELEAF);
        viewResolver.addStaticVariable("settings", themeSettingService.listAsMapBy(themeId));

        // TODO for freemarker
    }

    @EventListener
    public void onApplicationStartedEvent(ApplicationStartedEvent applicationStartedEvent) {
        themeService.fetchActivatedTheme().ifPresent(x -> {
            // load options
            loadOptions();
            // load menus
            loadMenus();
            // load owner
            loadUser();
            // load theme settings
            loadThemeSettings(x.getId());

            clearI18nCache();
            clearTemplateCache();
            clearViewCache();
        });
    }

    @EventListener
    public void onThemeActivatedEvent(ThemeActivatedEvent themeActivatedEvent) {
        themeService.fetchActivatedTheme().ifPresent(x -> {
            loadOptions();
            loadThemeSettings(x.getId());

            clearTemplateCache();
            clearViewCache();
        });
    }

    @EventListener
    public void onOptionUpdate(OptionUpdatedEvent event) {
        log.debug("Received option updated event");
        loadOptions();
        // default locale may change
        clearI18nCache();
        clearViewCache();
    }

    @EventListener
    public void onThemeUpdatedEvent(ThemeUpdatedEvent event) {
        log.debug("Received theme updated event");
        loadThemeSettings(themeService.getActivatedThemeId());
        // properties may change
        clearI18nCache();
        clearTemplateCache();
        clearViewCache();
    }

    @EventListener
    public void onUserUpdate(UserUpdatedEvent event) {
        log.debug("Received user updated event, user id: [{}]", event.getUserId());
        loadUser();
        clearViewCache();
    }

    @EventListener
    public void onMenuUpdate(MenuUpdateEvent event) {
        log.debug("Received menu updated event");
        loadMenus();
        clearViewCache();
    }

}