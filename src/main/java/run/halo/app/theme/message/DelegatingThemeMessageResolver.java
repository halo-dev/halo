package run.halo.app.theme.message;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.messageresolver.AbstractMessageResolver;
import run.halo.app.theme.ThemeContext;
import run.halo.app.theme.ThemeContextHolder;

/**
 * @author guqing
 * @since 2.0.0
 */
public class DelegatingThemeMessageResolver extends AbstractMessageResolver {
    private final Map<String, ThemeMessageResolver> delegateCache = new ConcurrentHashMap<>();

    @Override
    public String resolveMessage(ITemplateContext context, Class<?> origin, String key,
        Object[] messageParameters) {
        return getMessageResolver().resolveMessage(context, origin, key, messageParameters);
    }

    @Override
    public String createAbsentMessageRepresentation(ITemplateContext context, Class<?> origin,
        String key, Object[] messageParameters) {
        return getMessageResolver().createAbsentMessageRepresentation(context, origin, key,
            messageParameters);
    }

    private synchronized ThemeMessageResolver getMessageResolver() {
        ThemeContext themeContext = ThemeContextHolder.getThemeContext();
        String themeName = themeContext.getThemeName();
        ThemeMessageResolver themeMessageResolver = delegateCache.get(themeName);
        if (themeMessageResolver == null) {
            themeMessageResolver = new ThemeMessageResolver();
            delegateCache.put(themeName, themeMessageResolver);
        }
        return themeMessageResolver;
    }
}
