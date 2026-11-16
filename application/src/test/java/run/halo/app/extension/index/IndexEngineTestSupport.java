package run.halo.app.extension.index;

import org.springframework.core.convert.support.DefaultConversionService;

/**
 * Test-only factory that exposes a real {@link DefaultIndexEngine} (package-private) through the public
 * {@link IndexEngine} interface, so that tests in other packages can exercise real index behavior.
 */
public final class IndexEngineTestSupport {

    private IndexEngineTestSupport() {}

    public static IndexEngine newIndexEngine() {
        return new DefaultIndexEngine(new DefaultConversionService());
    }
}
