package run.halo.app.plugin;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * Tests for {@link SharedApplicationContextFactory}.
 *
 * @author guqing
 * @since 2.0.0
 */
@SpringBootTest
@AutoConfigureTestDatabase
class SharedApplicationContextFactoryTest {

    @Autowired
    ApplicationContext applicationContext;

    @Test
    void createSharedApplicationContext() {
        var sharedContext =  SharedApplicationContextFactory.create(applicationContext);
        assertNotNull(sharedContext);
    }
}