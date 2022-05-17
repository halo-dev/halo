package run.halo.app.extension;

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

}