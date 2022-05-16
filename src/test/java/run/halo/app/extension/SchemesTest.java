package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.exception.ExtensionException;

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
    }

    @Test
    void shouldFetchFakeWhenRegistered() {
        Schemes.INSTANCE.register(FakeExtension.class);

        var scheme = Schemes.INSTANCE.fetch(FakeExtension.class);
        assertTrue(scheme.isPresent());
    }
}