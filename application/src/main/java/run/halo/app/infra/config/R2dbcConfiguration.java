package run.halo.app.infra.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.mapping.R2dbcMappingContext;

@Configuration(proxyBeanMethods = false)
class R2dbcConfiguration {

    /**
     * Modify R2DBC Mapping Context to disable force quote.
     *
     * <p>
     * In Spring Boot 4, the default
     * behavior is changed to enable force quote, which may cause issues with existing database
     * schemas that do not use quoted identifiers.
     *
     * <p>
     * See
     * <a href="https://github.com/spring-projects/spring-data-relational/issues/1993">this issue
     * </a> for more details.
     *
     * <p>
     * Use static method to ensure that the BeanPostProcessor is registered before any
     * R2dbcMappingContext beans are initialized.
     *
     * @return the bean post processor
     */
    @Bean
    static BeanPostProcessor r2dbcMappingContextQuoteModifier() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName)
                throws BeansException {
                if (bean instanceof R2dbcMappingContext mappingContext) {
                    mappingContext.setForceQuote(false);
                }
                return bean;
            }
        };
    }

}
