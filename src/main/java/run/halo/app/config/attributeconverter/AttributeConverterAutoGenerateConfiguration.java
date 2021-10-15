package run.halo.app.config.attributeconverter;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryBuilderCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * Jpa configuration.
 *
 * @author johnniang
 */
public class AttributeConverterAutoGenerateConfiguration {

    @Bean
    EntityManagerFactoryBuilderCustomizer entityManagerFactoryBuilderCustomizer(
        ConfigurableListableBeanFactory factory) {
        return builder -> builder.setPersistenceUnitPostProcessors(
                new AutoGenerateConverterPersistenceUnitPostProcessor(factory));
    }
}
