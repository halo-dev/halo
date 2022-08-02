package run.halo.app.theme.newplan;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.spring6.ISpringWebFluxTemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.theme.engine.SpringWebFluxTemplateEngine;
import run.halo.app.theme.message.ThemeMessageResolver;

@Component
public class TemplateEngineManager {

    Map<String, ISpringWebFluxTemplateEngine> engineMap = new ConcurrentHashMap<>();

    private final ThymeleafProperties thymeleafProperties;

    private final ObjectProvider<ITemplateResolver> templateResolvers;

    private final ObjectProvider<IDialect> dialects;
    private final MessageSource messageSource;

    public TemplateEngineManager(ThymeleafProperties thymeleafProperties,
        HaloProperties haloProperties,
        ObjectProvider<ITemplateResolver> templateResolvers,
        ObjectProvider<IDialect> dialects,
        MessageSource messageSource) {
        this.thymeleafProperties = thymeleafProperties;
        this.templateResolvers = templateResolvers;
        this.dialects = dialects;
        this.messageSource = messageSource;
    }

    public ISpringWebFluxTemplateEngine getTemplateEngine(ThemeContext theme) {
        return engineMap.computeIfAbsent(theme.getName(), name -> {
            var engine = new SpringWebFluxTemplateEngine();
            engine.setEnableSpringELCompiler(thymeleafProperties.isEnableSpringElCompiler());
            engine.setMessageResolver(new ThemeMessageResolver(theme));
            engine.setMessageSource(messageSource);
            engine.addLinkBuilder(new ThemeLinkBuilder(theme));
            engine.setRenderHiddenMarkersBeforeCheckboxes(
                thymeleafProperties.isRenderHiddenMarkersBeforeCheckboxes());

            var mainResolver = haloTemplateResolver();
            mainResolver.setPrefix(theme.getPath() + "/templates/");
            engine.addTemplateResolver(mainResolver);

            templateResolvers.orderedStream().forEach(engine::addTemplateResolver);
            dialects.orderedStream().forEach(engine::addDialect);

            return engine;
        });
    }

    FileTemplateResolver haloTemplateResolver() {
        final var resolver = new FileTemplateResolver();
        resolver.setTemplateMode(thymeleafProperties.getMode());
        resolver.setPrefix(thymeleafProperties.getPrefix());
        resolver.setSuffix(thymeleafProperties.getSuffix());
        resolver.setCacheable(thymeleafProperties.isCache());
        resolver.setCheckExistence(thymeleafProperties.isCheckTemplate());
        if (thymeleafProperties.getEncoding() != null) {
            resolver.setCharacterEncoding(thymeleafProperties.getEncoding().name());
        }
        return resolver;
    }
}
