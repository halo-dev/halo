package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
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

    @Test
    void equalsAndHashCodeTest() {
        var scheme1 = Scheme.buildFromType(FakeExtension.class);
        var scheme2 = Scheme.buildFromType(FakeExtension.class);
        assertEquals(scheme1, scheme2);
        assertEquals(scheme1.hashCode(), scheme2.hashCode());

        // openApiSchema is not included in equals and hashCode.
        var scheme3 = new Scheme(FakeExtension.class, scheme1.groupVersionKind(),
            scheme1.plural(), scheme1.singular(), JsonNodeFactory.instance.objectNode());
        assertEquals(scheme1, scheme3);

        // singular and plural are not included in equals and hashCode.
        var scheme4 = new Scheme(FakeExtension.class, scheme1.groupVersionKind(),
            scheme1.plural(), "other", scheme1.openApiSchema());
        assertEquals(scheme1, scheme4);

        // plural is not included in equals and hashCode.
        var scheme5 = new Scheme(FakeExtension.class, scheme1.groupVersionKind(),
            "other", scheme1.singular(), scheme1.openApiSchema());
        assertEquals(scheme1, scheme5);

        // type is not included in equals and hashCode.
        var scheme6 = new Scheme(FakeExtension.class, scheme1.groupVersionKind(),
            scheme1.plural(), scheme1.singular(), scheme1.openApiSchema());
        assertEquals(scheme1, scheme6);

        // groupVersionKind is included in equals and hashCode.
        var scheme7 = new Scheme(FakeExtension.class,
            new GroupVersionKind("other.halo.run", "v1alpha1", "Fake"),
            scheme1.plural(), scheme1.singular(), scheme1.openApiSchema());
        assertNotEquals(scheme1, scheme7);

        scheme7 = new Scheme(FakeExtension.class,
            new GroupVersionKind("fake.halo.run", "v1alpha2", "Fake"),
            scheme1.plural(), scheme1.singular(), scheme1.openApiSchema());
        assertNotEquals(scheme1, scheme7);

        scheme7 = new Scheme(FakeExtension.class,
            new GroupVersionKind("fake.halo.run", "v1alpha1", "Other"),
            scheme1.plural(), scheme1.singular(), scheme1.openApiSchema());
        assertNotEquals(scheme1, scheme7);
    }
}