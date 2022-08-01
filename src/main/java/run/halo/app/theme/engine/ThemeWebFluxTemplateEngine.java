package run.halo.app.theme.engine;

import org.thymeleaf.spring6.ISpringWebFluxTemplateEngine;

/**
 * <p>
 * A implementation of {@link ISpringWebFluxTemplateEngine} for halo theme, and default
 * template engine implementation to be used in Spring WebFlux environments.
 * </p>
 *
 * @author guqing
 * @since 2.0.0
 */
public class ThemeWebFluxTemplateEngine extends SpringWebFluxTemplateEngine {

    @Override
    protected void initializeSpecific() {
        // Nothing to be executed here, override this method to remove parent initialization
    }
}
