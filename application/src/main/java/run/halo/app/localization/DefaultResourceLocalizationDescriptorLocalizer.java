package run.halo.app.localization;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import run.halo.app.plugin.PluginApplicationContext;
import run.halo.app.plugin.SpringPlugin;
import run.halo.app.plugin.SpringPluginManager;

/**
 * Resolves descriptor labels through the owning plugin's message source, Core, then the declared literal value.
 *
 * @since 2.28.0
 */
@Component
public class DefaultResourceLocalizationDescriptorLocalizer implements ResourceLocalizationDescriptorLocalizer {
    private final MessageSource coreMessages;
    private final SpringPluginManager pluginManager;

    public DefaultResourceLocalizationDescriptorLocalizer(
            MessageSource coreMessages, SpringPluginManager pluginManager) {
        this.coreMessages = Objects.requireNonNull(coreMessages);
        this.pluginManager = Objects.requireNonNull(pluginManager);
    }

    @Override
    public ResourceLocalizationDescriptor localize(
            ResourceLocalizationDescriptor descriptor, Class<?> owner, Locale locale) {
        return new LocalizedResourceLocalizationDescriptor(descriptor, value -> localizeLabel(value, owner, locale));
    }

    @Override
    public <T> LocalizableResourceDescriptor<T> localize(
            LocalizableResourceDescriptor<T> descriptor, Class<?> owner, Locale locale) {
        return new LocalizedLocalizableResourceDescriptor<>(descriptor, value -> localizeLabel(value, owner, locale));
    }

    @Override
    public String localizeLabel(String value, Class<?> owner, Locale locale) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        var ownerContext = ownerContext(owner);
        if (ownerContext.isPresent()) {
            var pluginMessage = ownerContext.get().getMessage(value, null, null, locale);
            if (pluginMessage != null) {
                return pluginMessage;
            }
        }
        var coreMessage = coreMessages.getMessage(value, null, null, locale);
        return coreMessage == null ? value : coreMessage;
    }

    private Optional<ApplicationContext> ownerContext(Class<?> owner) {
        if (owner == null || owner.getClassLoader() == null) {
            return Optional.empty();
        }
        for (var plugin : pluginManager.startedPlugins()) {
            if (plugin.getPlugin() instanceof SpringPlugin springPlugin) {
                var context = springPlugin.getApplicationContext();
                if (context instanceof PluginApplicationContext pluginContext
                        && pluginContext.getBeanFactory().getBeanClassLoader() == owner.getClassLoader()) {
                    return Optional.of(pluginContext);
                }
            }
        }
        return Optional.empty();
    }
}
