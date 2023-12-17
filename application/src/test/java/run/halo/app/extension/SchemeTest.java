package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

class SchemeTest {

    @Test
    void requiredFieldTest() {
        assertThrows(IllegalArgumentException.class,
            () -> new Scheme(null, new GroupVersionKind("", "v1alpha1", ""), "", "", null));
        assertThrows(IllegalArgumentException.class,
            () -> new Scheme(FakeExtension.class, new GroupVersionKind("", "", ""), "", "",
                null));
        assertThrows(IllegalArgumentException.class,
            () -> new Scheme(FakeExtension.class, new GroupVersionKind("", "v1alpha1", ""), "",
                "", null));
        assertThrows(IllegalArgumentException.class,
            () -> new Scheme(FakeExtension.class, new GroupVersionKind("", "v1alpha1", "Fake"), "",
                "", null));
        assertThrows(IllegalArgumentException.class,
            () -> new Scheme(FakeExtension.class, new GroupVersionKind("", "v1alpha1", "Fake"),
                "fakes", "", null));
        assertThrows(IllegalArgumentException.class, () -> {
            new Scheme(FakeExtension.class, new GroupVersionKind("", "v1alpha1", "Fake"), "fakes",
                "fake", null);
        });
        new Scheme(FakeExtension.class, new GroupVersionKind("", "v1alpha1", "Fake"), "fakes",
            "fake", new ObjectNode(null));
    }


    @Test
    void shouldThrowExceptionWhenTypeHasNoGvkAnno() {
        class NoGvkExtension extends AbstractExtension {
        }

        assertThrows(IllegalArgumentException.class,
            () -> Scheme.getGvkFromType(NoGvkExtension.class));
        assertThrows(IllegalArgumentException.class,
            () -> Scheme.buildFromType(NoGvkExtension.class));
    }

    @Test
    void shouldGetGvkFromTypeWithGvkAnno() {
        var gvk = Scheme.getGvkFromType(FakeExtension.class);
        assertEquals("fake.halo.run", gvk.group());
        assertEquals("v1alpha1", gvk.version());
        assertEquals("Fake", gvk.kind());
        assertEquals("fake", gvk.singular());
        assertEquals("fakes", gvk.plural());
    }

    @Test
    void shouldCreateSchemeSuccessfully() {
        var scheme = Scheme.buildFromType(FakeExtension.class);
        assertEquals(new GroupVersionKind("fake.halo.run", "v1alpha1", "Fake"),
            scheme.groupVersionKind());
        assertEquals("fake", scheme.singular());
        assertEquals("fakes", scheme.plural());
        assertNotNull(scheme.openApiSchema());
        assertEquals(FakeExtension.class, scheme.type());
    }

}