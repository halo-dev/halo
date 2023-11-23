package run.halo.app.plugin;

import lombok.experimental.UtilityClass;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.lang.Nullable;

/**
 * Utility class that allows for convenient registration of common bean post-processors for
 * {@link PluginApplicationContext}.
 *
 * @author guqing
 * @since 2.11.0
 */
@UtilityClass
public class BeanPostProcessorUtils {

    public static final String CUSTOM_ENDPOINT_PROCESSOR_BEAN_NAME =
        "run.halo.app.plugin.pluginCustomEndpointBeanFactoryPostProcessor";

    /**
     * Register all relevant annotation post-processors in the given registry.
     *
     * @param registry the registry to operate on
     */
    public static void registerBeanPostProcessors(BeanDefinitionRegistry registry) {
        registerBeanPostProcessors(registry, null);
    }

    /**
     * Register all relevant annotation post-processors in the given registry.
     *
     * @param registry the registry to operate on
     * @param source the configuration source element (already extracted)
     * that this registration was triggered from. May be {@code null}
     */
    public static void registerBeanPostProcessors(
        BeanDefinitionRegistry registry, @Nullable Object source) {

        if (!registry.containsBeanDefinition(CUSTOM_ENDPOINT_PROCESSOR_BEAN_NAME)) {
            RootBeanDefinition def = new RootBeanDefinition(
                PluginCustomEndpointBeanFactoryPostProcessor.class);
            def.setSource(source);
            def.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            registry.registerBeanDefinition(CUSTOM_ENDPOINT_PROCESSOR_BEAN_NAME, def);
        }
        // add more post processors
    }
}
