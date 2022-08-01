package run.halo.app.theme.message;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.messageresolver.AbstractMessageResolver;
import run.halo.app.theme.newplan.ThemeContext;

/**
 * @author guqing
 * @since 2.0.0
 */
public class DelegatingThemeMessageResolver extends AbstractMessageResolver {
    private final Map<String, ThemeMessageResolver> delegateCache = new HashMap<>();
    private final StampedLock stampedLock = new StampedLock();

    private final ThemeContext theme;

    public DelegatingThemeMessageResolver(ThemeContext theme) {
        this.theme = theme;
    }

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

    private ThemeMessageResolver getMessageResolver() {
        String themeName = theme.getName();
        long stamp = stampedLock.tryOptimisticRead();
        ThemeMessageResolver themeMessageResolver = delegateCache.get(themeName);
        if (themeMessageResolver == null) {
            return createMessageResolver(themeName);
        }
        if (!stampedLock.validate(stamp)) {
            stamp = stampedLock.readLock();
            try {
                return delegateCache.get(themeName);
            } finally {
                stampedLock.unlockRead(stamp);
            }
        }
        return themeMessageResolver;
    }

    private ThemeMessageResolver createMessageResolver(String themeName) {
        long stamp = stampedLock.writeLock();
        try {
            ThemeMessageResolver themeMessageResolver = new ThemeMessageResolver(theme);
            delegateCache.put(themeName, themeMessageResolver);
            return themeMessageResolver;
        } finally {
            stampedLock.unlock(stamp);
        }
    }
}
