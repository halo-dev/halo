package run.halo.app.config.attributeconverter;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jpa configuration.
 *
 * @author johnniang
 */
@Configuration(proxyBeanMethods = false)
public class JpaConfiguration {

    @Bean
    EntityManagerFactoryBuilderCustomizer entityManagerFactoryBuilderCustomizer(
        ConfigurableListableBeanFactory factory) {
        return builder -> builder.setPersistenceUnitPostProcessors(
            new AutoGenerateConverterPersistenceUnitPostProcessor());
    }
}
