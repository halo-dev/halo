package run.halo.app.theme.finders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

/**
 * Tests for {@link FinderRegistry}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class FinderRegistryTest {

    private DefaultFinderRegistry finderRegistry;
    @Mock
    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        finderRegistry = new DefaultFinderRegistry(applicationContext);
    }

    @Test
    void registerFinder() {
        assertThatThrownBy(() -> {
            finderRegistry.putFinder(new Object());
        }).isInstanceOf(IllegalStateException.class)
            .hasMessage("Finder must be annotated with @Finder");

        String s = finderRegistry.putFinder(new FakeFinder());
        assertThat(s).isEqualTo("test");
    }

    @Test
    void removeFinder() {
        String s = finderRegistry.putFinder(new FakeFinder());
        assertThat(s).isEqualTo("test");
        Object test = finderRegistry.get("test");
        assertThat(test).isNotNull();
        finderRegistry.removeFinder(s);

        test = finderRegistry.get("test");
        assertThat(test).isNull();
    }

    @Test
    void getFinders() {
        assertThat(finderRegistry.getFinders()).hasSize(0);

        finderRegistry.putFinder(new FakeFinder());
        Map<String, Object> finders = finderRegistry.getFinders();
        assertThat(finders).hasSize(1);
    }

    @Finder("test")
    static class FakeFinder {

    }
}