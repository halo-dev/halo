package run.halo.app.extension.router;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.router.ExtensionRouterFunctionFactory.PathPatternGenerator;

class PathPatternGeneratorTest {

    @GVK(group = "fake.halo.run", version = "v1alpha1", kind = "Fake",
        singular = "fake", plural = "fakes")
    private static class GroupExtension extends AbstractExtension {
    }

    @GVK(group = "", version = "v1alpha1", kind = "Fake",
        singular = "fake", plural = "fakes")
    private static class GrouplessExtension extends AbstractExtension {
    }

    @Test
    void buildGroupedExtensionPathPattern() {
        var scheme = Scheme.buildFromType(GroupExtension.class);
        var pathPattern = PathPatternGenerator.buildExtensionPathPattern(scheme);
        assertEquals("/apis/fake.halo.run/v1alpha1/fakes", pathPattern);
    }

    @Test
    void buildGrouplessExtensionPathPattern() {
        var scheme = Scheme.buildFromType(GrouplessExtension.class);
        var pathPattern = PathPatternGenerator.buildExtensionPathPattern(scheme);
        assertEquals("/api/v1alpha1/fakes", pathPattern);
    }
}