package run.halo.app.theme.cache;

import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.cache.ICache;
import org.thymeleaf.cache.ICacheEntryValidityChecker;
import org.thymeleaf.cache.StandardCache;
import org.thymeleaf.cache.TemplateCacheKey;
import org.thymeleaf.engine.TemplateModel;
import run.halo.app.theme.ThemeContext;
import run.halo.app.theme.ThemeContextHolder;

/**
 * @author guqing
 * @since 2.0.0
 */
public class ThemeTemplateCacheDecorator implements ICache<TemplateCacheKey, TemplateModel> {
    private final StandardCache<TemplateCacheKey, TemplateModel> standardCache;

    public ThemeTemplateCacheDecorator(
        StandardCache<TemplateCacheKey, TemplateModel> standardCache) {
        this.standardCache = standardCache;
    }

    @Override
    public void put(TemplateCacheKey key, TemplateModel value) {
        standardCache.put(decorateCacheKey(key), value);
    }

    @Override
    public TemplateModel get(TemplateCacheKey key) {
        return standardCache.get(decorateCacheKey(key));
    }

    @Override
    public TemplateModel get(TemplateCacheKey key,
        ICacheEntryValidityChecker<? super TemplateCacheKey, ? super
            TemplateModel> validityChecker) {
        return standardCache.get(decorateCacheKey(key), validityChecker);
    }

    @Override
    public void clear() {
        standardCache.clear();
    }

    @Override
    public void clearKey(TemplateCacheKey key) {
        standardCache.clearKey(decorateCacheKey(key));
    }

    @Override
    public Set<TemplateCacheKey> keySet() {
        return standardCache.keySet()
            .stream()
            .map(this::cancelDecorate)
            .collect(Collectors.toSet());
    }

    private TemplateCacheKey decorateCacheKey(TemplateCacheKey key) {
        ThemeContext themeContext = ThemeContextHolder.getThemeContext();
        String themeName = themeContext.getThemeName();
        String template = key.getTemplate();
        String decoratedTemplate = getKeyPrefix(themeName) + template;
        return changeCacheKey(key, decoratedTemplate);
    }

    private TemplateCacheKey changeCacheKey(TemplateCacheKey key, String newTemplate) {
        return new TemplateCacheKey(key.getOwnerTemplate(), newTemplate,
            key.getTemplateSelectors(), key.getLineOffset(), key.getColOffset(),
            key.getTemplateMode(), key.getTemplateResolutionAttributes());
    }

    private TemplateCacheKey cancelDecorate(TemplateCacheKey key) {
        ThemeContext themeContext = ThemeContextHolder.getThemeContext();
        String themeName = themeContext.getThemeName();
        String template = key.getTemplate();
        String newTemplate = StringUtils.removeStart(template, getKeyPrefix(themeName));
        return changeCacheKey(key, newTemplate);
    }

    private String getKeyPrefix(String themeName) {
        return themeName + ":";
    }
}
