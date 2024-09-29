package run.halo.app.theme.message;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.thymeleaf.messageresolver.StandardMessageResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import run.halo.app.theme.ThemeContext;

/**
 * @author guqing
 * @since 2.0.0
 */
public class ThemeMessageResolver extends StandardMessageResolver {

    private final ThemeContext theme;

    public ThemeMessageResolver(ThemeContext theme) {
        this.theme = theme;
    }

    @Override
    protected Map<String, String> resolveMessagesForTemplate(String template,
        ITemplateResource templateResource,
        Locale locale) {
        var properties = new HashMap<String, String>();
        Optional.ofNullable(ThemeMessageResolutionUtils.resolveMessagesForTemplate(locale, theme))
            .ifPresent(properties::putAll);
        Optional.ofNullable(super.resolveMessagesForTemplate(template, templateResource, locale))
            .ifPresent(properties::putAll);
        return Collections.unmodifiableMap(properties);
    }

}
