package run.halo.app.extension.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.FakeExtension;

@ExtendWith(MockitoExtension.class)
class ControllerBuilderTest {

    @Mock
    ExtensionClient client;

    @Test
    void buildWithNullReconciler() {
        assertThrows(IllegalArgumentException.class,
            () -> new ControllerBuilder(null, client).build(), "Reconciler must not be null");
    }

    @Test
    void buildWithNullClient() {
        assertThrows(IllegalArgumentException.class,
            () -> new ControllerBuilder(new FakeReconciler(), null).build());
    }

    @Test
    void buildTest() {
        assertThrows(IllegalArgumentException.class,
            () -> new ControllerBuilder(new FakeReconciler(), client)
                .build(),
            "Extension must not be null");

        assertNotNull(fakeBuilder().build());

        assertNotNull(fakeBuilder()
            .syncAllOnStart(true)
            .nowSupplier(Instant::now)
            .minDelay(Duration.ofMillis(5))
            .maxDelay(Duration.ofSeconds(1000))
            .build());

        assertNotNull(fakeBuilder()
            .syncAllOnStart(true)
            .minDelay(Duration.ofMillis(5))
            .maxDelay(Duration.ofSeconds(1000))
            .onAddPredicate(Objects::nonNull)
            .onUpdatePredicate(Objects::equals)
            .onDeletePredicate(Objects::nonNull)
            .build()
        );
    }

    @Test
    void invalidMinDelayAndMaxDelay() {
        assertThrows(IllegalArgumentException.class,
            () -> fakeBuilder()
                .minDelay(Duration.ofSeconds(2))
                .maxDelay(Duration.ofSeconds(1))
                .build(),
            "Min delay must be less than or equal to max delay");

        assertNotNull(fakeBuilder()
            .minDelay(null)
            .maxDelay(Duration.ofSeconds(1))
            .build());

        assertNotNull(fakeBuilder()
            .minDelay(Duration.ofSeconds(1))
            .maxDelay(null)
            .build());

        assertNotNull(fakeBuilder()
            .minDelay(Duration.ofSeconds(-1))
            .build());

        assertNotNull(fakeBuilder()
            .maxDelay(Duration.ofSeconds(-1))
            .build());
    }

    ControllerBuilder fakeBuilder() {
        return new ControllerBuilder(new FakeReconciler(), client)
            .extension(new FakeExtension());
    }

    static class FakeReconciler implements Reconciler<Reconciler.Request> {

        @Override
        public Result reconcile(Request request) {
            return new Reconciler.Result(false, null);
        }

        @Override
        public Controller setupWith(ControllerBuilder builder) {
            return null;
        }
    }

}