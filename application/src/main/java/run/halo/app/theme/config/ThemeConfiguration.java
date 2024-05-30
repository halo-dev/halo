package run.halo.app.theme.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
import run.halo.app.theme.dialect.GeneratorMetaProcessor;
import run.halo.app.theme.dialect.HaloSpringSecurityDialect;
import run.halo.app.theme.dialect.LinkExpressionObjectDialect;
import run.halo.app.theme.dialect.TemplateHeadProcessor;

/**
 * @author guqing
 * @since 2.0.0
 */
@Configuration
public class ThemeConfiguration {

    @Bean
    LinkExpressionObjectDialect linkExpressionObjectDialect() {
        return new LinkExpressionObjectDialect();
    }

    @Bean
    SpringSecurityDialect springSecurityDialect(
        ServerSecurityContextRepository securityContextRepository) {
        return new HaloSpringSecurityDialect(securityContextRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "halo.theme.generator-meta-disabled",
        havingValue = "false",
        matchIfMissing = true)
    TemplateHeadProcessor generatorMetaProcessor(ObjectProvider<BuildProperties> buildProperties) {
        return new GeneratorMetaProcessor(buildProperties);
    }
}
