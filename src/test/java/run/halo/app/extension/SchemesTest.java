package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static run.halo.app.extension.GroupVersionKind.fromAPIVersionAndKind;

import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.exception.ExtensionException;
import run.halo.app.extension.exception.SchemeNotFoundException;

class SchemesTest {


    @AfterEach
    void cleanUp() {
        Schemes.INSTANCE.clear();
    }

    @Test
    void testRegister() {
        Schemes.INSTANCE.register(FakeExtension.class);
    }

    @Test
    void shouldThrowExceptionWithoutGVKAnnotation() {
        class WithoutGVKExtension extends AbstractExtension {
        }

        assertThrows(ExtensionException.class,
            () -> Schemes.INSTANCE.register(WithoutGVKExtension.class));
    }

    @Test
    void shouldFetchNothingWhenUnregistered() {
        var scheme = Schemes.INSTANCE.fetch(FakeExtension.class);
        assertEquals(Optional.empty(), scheme);
        assertThrows(SchemeNotFoundException.class,
            () -> Schemes.INSTANCE.get(FakeExtension.class));

        var gvk = fromAPIVersionAndKind("fake.halo.run/v1alpha1", "Fake");
        scheme = Schemes.INSTANCE.fetch(gvk);
        assertEquals(Optional.empty(), scheme);
        assertThrows(SchemeNotFoundException.class, () -> Schemes.INSTANCE.get(gvk));
    }

    @Test
    void shouldFetchFakeWhenRegistered() {
        Schemes.INSTANCE.register(FakeExtension.class);

        var scheme = Schemes.INSTANCE.fetch(FakeExtension.class);
        assertTrue(scheme.isPresent());

        scheme = Schemes.INSTANCE.fetch(fromAPIVersionAndKind("fake.halo.run/v1alpha1", "Fake"));
        assertTrue(scheme.isPresent());
    }
}