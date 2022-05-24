package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AbstractExtensionTest {

    @Test
    void groupVersionKind() {
        var extension = new AbstractExtension() {
        };
        extension.setApiVersion("fake.halo.run/v1alpha1");
        extension.setKind("Fake");
        var gvk = extension.groupVersionKind();

        assertEquals(new GroupVersionKind("fake.halo.run", "v1alpha1", "Fake"), gvk);
    }

    @Test
    void testGroupVersionKind() {
        var extension = new AbstractExtension() {
        };
        extension.groupVersionKind(new GroupVersionKind("fake.halo.run", "v1alpha1", "Fake"));

        assertEquals("fake.halo.run/v1alpha1", extension.getApiVersion());
        assertEquals("Fake", extension.getKind());
    }

    @Test
    void metadata() {
        var extension = new AbstractExtension() {
        };
        Metadata metadata = new Metadata();
        metadata.setName("fake");
        extension.setMetadata(metadata);

        assertEquals(metadata, extension.getMetadata());
    }

    @Test
    void testMetadata() {
        var extension = new AbstractExtension() {
        };

        Metadata metadata = new Metadata();
        metadata.setName("fake");
        extension.setMetadata(metadata);

        assertEquals(metadata, extension.getMetadata());
    }
}