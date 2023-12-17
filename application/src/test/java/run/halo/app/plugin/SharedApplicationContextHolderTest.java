package run.halo.app.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Tests for {@link SharedApplicationContextHolder}.
 *
 * @author guqing
 * @since 2.0.0
 */
@SpringBootTest
@AutoConfigureTestDatabase
class SharedApplicationContextHolderTest {

    @Autowired
    SharedApplicationContextHolder sharedApplicationContextHolder;

    @Test
    void getInstance() {
        SharedApplicationContext instance1 = sharedApplicationContextHolder.getInstance();
        SharedApplicationContext instance2 = sharedApplicationContextHolder.getInstance();
        assertThat(instance1).isNotNull();
        assertThat(instance1).isEqualTo(instance2);
    }

    @Test
    void createSharedApplicationContext() {
        SharedApplicationContext sharedApplicationContext =
            sharedApplicationContextHolder.createSharedApplicationContext();
        assertThat(sharedApplicationContext).isNotNull();
    }
}