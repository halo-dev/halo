package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static run.halo.app.extension.GroupVersionKind.fromAPIVersionAndKind;

import java.util.List;
import org.junit.jupiter.api.Test;

class GroupVersionKindTest {

    @Test
    void testFromApiVersionAndKind() {
        record TestCase(String apiVersion, String kind, GroupVersionKind expected,
                        Class<? extends Throwable> exception) {
        }

        List.of(
            new TestCase("v1alpha1", "Fake", new GroupVersionKind("", "v1alpha1", "Fake"), null),
            new TestCase("fake.halo.run/v1alpha1", "Fake",
                new GroupVersionKind("fake.halo.run", "v1alpha1", "Fake"), null),
            new TestCase("", "", null, IllegalArgumentException.class),
            new TestCase("", "Fake", null, IllegalArgumentException.class),
            new TestCase("v1alpha1", "", null, IllegalArgumentException.class),
            new TestCase("fake.halo.run/v1alpha1/v1alpha2", "Fake", null,
                IllegalArgumentException.class)
        ).forEach(testCase -> {
            if (testCase.exception != null) {
                assertThrows(testCase.exception, () -> {
                    fromAPIVersionAndKind(testCase.apiVersion, testCase.kind);
                });
            } else {
                var got = fromAPIVersionAndKind(testCase.apiVersion, testCase.kind);
                assertEquals(testCase.expected, got);
            }
        });
    }

    @Test
    void testHasGroup() {
        record TestCase(GroupVersionKind gvk, boolean hasGroup) {
        }

        List.of(
            new TestCase(new GroupVersionKind("", "v1alpha1", "Fake"), false),
            new TestCase(new GroupVersionKind("fake.halo.run", "v1alpha1", "Fake"), true)
        ).forEach(testCase -> assertEquals(testCase.hasGroup, testCase.gvk.hasGroup()));
    }

    @Test
    void testGroupKind() {
        record TestCase(GroupVersionKind gvk, GroupKind gk) {
        }

        List.of(
            new TestCase(new GroupVersionKind("", "v1alpha1", "Fake"), new GroupKind("", "Fake")),
            new TestCase(new GroupVersionKind("fake.halo.run", "v1alpha1", "Fake"),
                new GroupKind("fake.halo.run", "Fake"))
        ).forEach(testCase -> {
            assertEquals(testCase.gk, testCase.gvk.groupKind());
        });
    }

    @Test
    void testGroupVersion() {
        record TestCase(GroupVersionKind gvk, GroupVersion gv) {
        }

        List.of(
            new TestCase(new GroupVersionKind("", "v1alpha1", "Fake"),
                new GroupVersion("", "v1alpha1")),
            new TestCase(new GroupVersionKind("fake.halo.run", "v1alpha1", "Fake"),
                new GroupVersion("fake.halo.run", "v1alpha1"))
        ).forEach(testCase -> {
            assertEquals(testCase.gv, testCase.gvk.groupVersion());
        });
    }

    @Test
    void fromExtension() {
        GroupVersionKind groupVersionKind = GroupVersionKind.fromExtension(FakeExtension.class);
        assertEquals("fake.halo.run", groupVersionKind.group());
        assertEquals("v1alpha1", groupVersionKind.version());
        assertEquals("Fake", groupVersionKind.kind());
    }

}