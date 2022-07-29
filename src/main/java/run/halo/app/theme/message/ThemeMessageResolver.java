package run.halo.app.theme.message;

import java.util.Locale;
import java.util.Map;
import org.thymeleaf.messageresolver.StandardMessageResolver;
import org.thymeleaf.templateresource.ITemplateResource;

/**
 * @author guqing
 * @since 2.0.0
 */
public class ThemeMessageResolver extends StandardMessageResolver {

    @Override
    protected Map<String, String> resolveMessagesForTemplate(String template,
        ITemplateResource templateResource,
        Locale locale) {
        return ThemeMessageResolutionUtils.resolveMessagesForTemplate(locale);
    }

    @Override
    protected Map<String, String> resolveMessagesForOrigin(Class<?> origin, Locale locale) {
        return ThemeMessageResolutionUtils.resolveMessagesForOrigin(origin, locale);
    }

    @Override
    protected String formatMessage(Locale locale, String message, Object[] messageParameters) {
        return ThemeMessageResolutionUtils.formatMessage(locale, message, messageParameters);
    }
}
