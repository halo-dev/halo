package run.halo.app.plugin;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.server.RouterFunction;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.core.extension.endpoint.CustomEndpointsBuilder;

/**
 * <p>A {@link CustomEndpoint} initialization {@link BeanFactoryPostProcessor} to build
 * {@link RouterFunction}s to bean factory.</p>
 *
 * @author guqing
 * @since 2.11.0
 */
public class PluginCustomEndpointBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory)
        throws BeansException {
        String[] customEndpointBeans = beanFactory.getBeanNamesForType(CustomEndpoint.class);
        if (customEndpointBeans.length == 0) {
            return;
        }
        CustomEndpointsBuilder endpointBuilder = new CustomEndpointsBuilder();
        for (String beanName : customEndpointBeans) {
            CustomEndpoint customEndpoint = (CustomEndpoint) beanFactory.getBean(beanName);
            endpointBuilder.add(customEndpoint);
        }
        beanFactory.registerSingleton("pluginCustomEndpointRouter", endpointBuilder.build());
    }
}
