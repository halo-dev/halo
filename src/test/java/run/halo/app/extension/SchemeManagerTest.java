package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import run.halo.app.extension.exception.ExtensionException;

class SchemeManagerTest {

    @Test
    void shouldThrowExceptionWhenTypeHasNoGvkAnno() {
        class NoGvkExtension extends AbstractExtension {
        }

        assertThrows(ExtensionException.class,
            () -> SchemeManager.getGvkFromType(NoGvkExtension.class));
        assertThrows(ExtensionException.class,
            () -> SchemeManager.createSchemeFromType(NoGvkExtension.class));
    }

    @Test
    void shouldGetGvkFromTypeWithGvkAnno() {
        var gvk = SchemeManager.getGvkFromType(FakeExtension.class);
        assertEquals("fake.halo.run", gvk.group());
        assertEquals("v1alpha1", gvk.version());
        assertEquals("Fake", gvk.kind());
        assertEquals("fake", gvk.singular());
        assertEquals("fakes", gvk.plural());
    }

    @Test
    void shouldCreateSchemeSuccessfully() {
        var scheme = SchemeManager.createSchemeFromType(FakeExtension.class);
        assertEquals(new GroupVersionKind("fake.halo.run", "v1alpha1", "Fake"),
            scheme.groupVersionKind());
        assertEquals("fake", scheme.singular());
        assertEquals("fakes", scheme.plural());
        assertNotNull(scheme.jsonSchema());
        assertEquals(FakeExtension.class, scheme.type());
    }

}