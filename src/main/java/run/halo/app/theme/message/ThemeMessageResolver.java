package run.halo.app.theme.message;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.TemplateData;
import org.thymeleaf.messageresolver.AbstractMessageResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.util.Validate;
import run.halo.app.theme.ThemeContext;
import run.halo.app.theme.ThemeContextHolder;

/**
 * <p>Each theme has its own language file in i18n directory.
 * For example, messages in default theme for locale:
 * {@code gl_ES-gheada} ("gl" = language, "ES" = country, "gheada" = variant) would be looked for
 * in {@code .properties} files in the following sequence:</p>
 * <ul>
 *   <li>{@code default/i18n/gl_ES-gheada.properties}</li>
 *   <li>{@code default/i18n/gl_ES.properties}</li>
 *   <li>{@code default/i18n/gl.properties}</li>
 *   <li>{@code default/i18n/default.properties}</li>
 * </ul>
 *
 * @author guqing
 * @see org.thymeleaf.messageresolver.StandardMessageResolver
 * @since 2.0
 */
@Slf4j
public class ThemeMessageResolver extends AbstractMessageResolver {
    private final ConcurrentHashMap<TemplateCacheKey, ConcurrentHashMap<Locale, Map<String,
        String>>>
        messagesByLocaleByTemplate =
        new ConcurrentHashMap<>(20, 0.9f, 2);
    private final ConcurrentHashMap<OriginCacheKey, ConcurrentHashMap<Locale, Map<String, String>>>
        messagesByLocaleByOrigin =
        new ConcurrentHashMap<>(20, 0.9f,
            2);
    private final Properties defaultMessages;


    public ThemeMessageResolver() {
        super();
        this.defaultMessages = new Properties();
    }


    /**
     * <p>
     * Sets the default messages. These messages will be used
     * if no other messages can be found.
     * </p>
     *
     * @param defaultMessages the new default messages
     */
    public final void setDefaultMessages(final Properties defaultMessages) {
        if (defaultMessages != null) {
            this.defaultMessages.putAll(defaultMessages);
        }
    }


    /**
     * <p>
     * Adds a new message to the set of default messages.
     * </p>
     *
     * @param key the message key
     * @param value the message value (text)
     */
    public final void addDefaultMessage(final String key, final String value) {
        Validate.notNull(key, "Key for default message cannot be null");
        Validate.notNull(value, "Value for default message cannot be null");
        this.defaultMessages.put(key, value);
    }


    /**
     * <p>
     * Clears the set of default messages.
     * </p>
     */
    public final void clearDefaultMessages() {
        this.defaultMessages.clear();
    }

    @Override
    public String resolveMessage(
        final ITemplateContext context, final Class<?> origin, final String key,
        final Object[] messageParameters) {
        return resolveMessage(context, origin, key, messageParameters, true, true, true);
    }

    public String resolveMessage(
        final ITemplateContext context, final Class<?> origin, final String key,
        final Object[] messageParameters,
        final boolean performTemplateBasedResolution, final boolean performOriginBasedResolution,
        final boolean performDefaultBasedResolution) {

        Validate.notNull(context, "Context cannot be null");
        Validate.notNull(context.getLocale(), "Locale in context cannot be null");
        Validate.notNull(key, "Message key cannot be null");

        final Locale locale = context.getLocale();

        /*
         * FIRST STEP: Look for the message using template-based resolution
         *
         * Note that resolution is top-down, this is, starts at the first-level template (the one
         *  being executed)
         * and only if a key is not found will try resolving for nested templates in the order
         * they have been nested.
         *
         * This allows container templates to override the messages defined in fragments, which
         * will act as defaults.
         */
        if (performTemplateBasedResolution) {

            for (final TemplateData templateData : context.getTemplateStack()) {

                final String template = templateData.getTemplate();
                final ITemplateResource templateResource = templateData.getTemplateResource();
                final boolean templateCacheable = templateData.getValidity().isCacheable();

                Map<String, String> messagesForLocaleForTemplate;

                // We will ONLY cache messages for cacheable templates. This should adequately
                // control cache growth
                if (templateCacheable) {

                    ConcurrentHashMap<Locale, Map<String, String>> messagesByLocaleForTemplate =
                        this.messagesByLocaleByTemplate.get(TemplateCacheKey.of(template));
                    if (messagesByLocaleForTemplate == null) {
                        this.messagesByLocaleByTemplate.putIfAbsent(TemplateCacheKey.of(template),
                            new ConcurrentHashMap<>(4));
                        messagesByLocaleForTemplate =
                            this.messagesByLocaleByTemplate.get(TemplateCacheKey.of(template));
                    }

                    messagesForLocaleForTemplate = messagesByLocaleForTemplate.get(locale);
                    if (messagesForLocaleForTemplate == null) {
                        messagesForLocaleForTemplate =
                            resolveMessagesForTemplate(template, templateResource, locale);
                        if (messagesForLocaleForTemplate == null) {
                            messagesForLocaleForTemplate = Collections.emptyMap();
                        }
                        messagesByLocaleForTemplate.putIfAbsent(locale,
                            messagesForLocaleForTemplate);
                        // We retrieve it again in order to be sure its the stored map (because
                        // of the 'putIfAbsent')
                        messagesForLocaleForTemplate = messagesByLocaleForTemplate.get(locale);
                    }

                } else {

                    messagesForLocaleForTemplate =
                        resolveMessagesForTemplate(template, templateResource, locale);
                    if (messagesForLocaleForTemplate == null) {
                        messagesForLocaleForTemplate = Collections.emptyMap();
                    }

                }

                // Once the messages map has been retrieved, just use it
                final String message = messagesForLocaleForTemplate.get(key);
                if (message != null) {
                    return formatMessage(locale, message, messageParameters);
                }

                // Will try the next resolver (if any)

            }

        }


        /*
         * SECOND STEP: Look for the message using origin-based resolution
         */
        if (performOriginBasedResolution && origin != null) {

            ConcurrentHashMap<Locale, Map<String, String>> messagesByLocaleForOrigin =
                this.messagesByLocaleByOrigin.get(OriginCacheKey.of(origin));
            if (messagesByLocaleForOrigin == null) {
                this.messagesByLocaleByOrigin.putIfAbsent(OriginCacheKey.of(origin),
                    new ConcurrentHashMap<>(4));
                messagesByLocaleForOrigin =
                    this.messagesByLocaleByOrigin.get(OriginCacheKey.of(origin));
            }

            Map<String, String> messagesForLocaleForOrigin = messagesByLocaleForOrigin.get(locale);
            if (messagesForLocaleForOrigin == null) {
                messagesForLocaleForOrigin = resolveMessagesForOrigin(origin, locale);
                if (messagesForLocaleForOrigin == null) {
                    messagesForLocaleForOrigin = Collections.emptyMap();
                }
                messagesByLocaleForOrigin.putIfAbsent(locale, messagesForLocaleForOrigin);
                // We retrieve it again in order to be sure it's the stored map (because of the
                // 'putIfAbsent')
                messagesForLocaleForOrigin = messagesByLocaleForOrigin.get(locale);
            }

            // Once the messages map has been retrieved, just use it
            final String message = messagesForLocaleForOrigin.get(key);
            if (message != null) {
                return formatMessage(locale, message, messageParameters);
            }

        }

        /*
         * THIRD STEP: Try default messages.
         */
        if (performDefaultBasedResolution && this.defaultMessages != null) {

            final String message = this.defaultMessages.getProperty(key);
            if (message != null) {
                return formatMessage(locale, message, messageParameters);
            }
        }


        /*
         * NOT FOUND, return null
         */
        return null;
    }

    protected Map<String, String> resolveMessagesForTemplate(String template,
        ITemplateResource templateResource,
        Locale locale) {
        return ThemeMessageResolutionUtils.resolveMessagesForTemplate(locale);
    }

    protected Map<String, String> resolveMessagesForOrigin(Class<?> origin, Locale locale) {
        return ThemeMessageResolutionUtils.resolveMessagesForOrigin(origin, locale);
    }

    protected String formatMessage(Locale locale, String message, Object[] messageParameters) {
        return ThemeMessageResolutionUtils.formatMessage(locale, message, messageParameters);
    }

    @Override
    public String createAbsentMessageRepresentation(
        final ITemplateContext context, final Class<?> origin, final String key,
        final Object[] messageParameters) {
        Validate.notNull(key, "Message key cannot be null");
        if (context.getLocale() != null) {
            return "??" + key + "_" + context.getLocale().toString() + "??";
        }
        return "??" + key + "_" + "??";
    }

    record OriginCacheKey(String themeName, Class<?> origin) {
        static OriginCacheKey of(Class<?> origin) {
            ThemeContext themeContext = ThemeContextHolder.getThemeContext();
            String themeName = themeContext.getThemeName();
            return new OriginCacheKey(themeName, origin);
        }
    }

    record TemplateCacheKey(String themeName, String template) {
        static TemplateCacheKey of(String template) {
            ThemeContext themeContext = ThemeContextHolder.getThemeContext();
            String themeName = themeContext.getThemeName();
            return new TemplateCacheKey(themeName, template);
        }
    }
}
