package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExtensionUtilTest {

    Scheme scheme;

    Scheme grouplessScheme;

    @BeforeEach
    void setUp() {
        scheme = new Scheme(FakeExtension.class,
            new GroupVersionKind("fake.halo.run", "v1alpha1", "Fake"),
            "fakes",
            "fake",
            new ObjectNode(null));
        grouplessScheme = new Scheme(FakeExtension.class,
            new GroupVersionKind("", "v1alpha1", "Fake"),
            "fakes",
            "fake",
            new ObjectNode(null));
    }

    @Test
    void buildStoreNamePrefix() {
        var prefix = ExtensionUtil.buildStoreNamePrefix(scheme);
        assertEquals("/registry/fake.halo.run/fakes", prefix);

        prefix = ExtensionUtil.buildStoreNamePrefix(grouplessScheme);
        assertEquals("/registry/fakes", prefix);
    }

    @Test
    void buildStoreName() {
        var storeName = ExtensionUtil.buildStoreName(scheme, "fake-name");
        assertEquals("/registry/fake.halo.run/fakes/fake-name", storeName);

        storeName = ExtensionUtil.buildStoreName(grouplessScheme, "fake-name");
        assertEquals("/registry/fakes/fake-name", storeName);
    }

}