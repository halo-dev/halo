package run.halo.app.theme;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.ConcurrentLruCache;
import org.springframework.util.ResourceUtils;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.spring6.ISpringWebFluxTemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.theme.dialect.HaloProcessorDialect;
import run.halo.app.theme.engine.SpringWebFluxTemplateEngine;
import run.halo.app.theme.message.ThemeMessageResolver;

/**
 * <p>The {@link TemplateEngineManager} uses an {@link ConcurrentLruCache LRU cache} to manage
 * theme's {@link ISpringWebFluxTemplateEngine}.</p>
 * <p>The default limit size of the {@link ConcurrentLruCache LRU cache} is
 * {@link TemplateEngineManager#CACHE_SIZE_LIMIT} to prevent unnecessary memory occupation.</p>
 * <p>If theme's {@link ISpringWebFluxTemplateEngine} already exists, it returns.</p>
 * <p>Otherwise, it checks whether the theme exists and creates the
 * {@link ISpringWebFluxTemplateEngine} into the LRU cache according to the {@link ThemeContext}
 * .</p>
 * <p>It is thread safe.</p>
 *
 * @author johnniang
 * @author guqing
 * @since 2.0.0
 */
@Component
public class TemplateEngineManager {
    private static final int CACHE_SIZE_LIMIT = 5;
    private final ConcurrentLruCache<ThemeContext, ISpringWebFluxTemplateEngine> engineCache;

    private final ThymeleafProperties thymeleafProperties;

    private final ObjectProvider<ITemplateResolver> templateResolvers;

    private final ObjectProvider<IDialect> dialects;

    public TemplateEngineManager(ThymeleafProperties thymeleafProperties,
        ObjectProvider<ITemplateResolver> templateResolvers,
        ObjectProvider<IDialect> dialects) {
        this.thymeleafProperties = thymeleafProperties;
        this.templateResolvers = templateResolvers;
        this.dialects = dialects;
        engineCache = new ConcurrentLruCache<>(CACHE_SIZE_LIMIT, this::templateEngineGenerator);
    }

    public ISpringWebFluxTemplateEngine getTemplateEngine(ThemeContext theme) {
        // cache not exists, will create new engine
        if (!engineCache.contains(theme)) {
            // before this, check if theme exists
            if (!fileExists(theme.getPath())) {
                throw new NotFoundException("Theme not found.");
            }
        }
        return engineCache.get(theme);
    }

    private boolean fileExists(Path path) {
        try {
            return ResourceUtils.getFile(path.toUri()).exists();
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    private ISpringWebFluxTemplateEngine templateEngineGenerator(ThemeContext theme) {
        var engine = new SpringWebFluxTemplateEngine();
        engine.setEnableSpringELCompiler(thymeleafProperties.isEnableSpringElCompiler());
        engine.setMessageResolver(new ThemeMessageResolver(theme));
        engine.setLinkBuilder(new ThemeLinkBuilder(theme));
        engine.setRenderHiddenMarkersBeforeCheckboxes(
            thymeleafProperties.isRenderHiddenMarkersBeforeCheckboxes());

        var mainResolver = haloTemplateResolver();
        mainResolver.setPrefix(theme.getPath() + "/templates/");
        engine.addTemplateResolver(mainResolver);
        engine.addDialect(new HaloProcessorDialect());

        templateResolvers.orderedStream().forEach(engine::addTemplateResolver);
        dialects.orderedStream().forEach(engine::addDialect);

        return engine;
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
