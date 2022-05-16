package run.halo.app.extension;

import org.junit.jupiter.api.Test;

class SchemesTest {

    @Test
    void testRegister() {
        Schemes.INSTANCE.register(FakeExtension.class);
    }

}