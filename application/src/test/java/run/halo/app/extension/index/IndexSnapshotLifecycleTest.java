package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import run.halo.app.extension.DefaultSchemeManager;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.store.ExtensionStoreRepository;
import run.halo.app.infra.InitializationPhase;
import run.halo.app.plugin.SpringPluginManager;

class IndexSnapshotLifecycleTest {
    @TempDir
    Path workDir;

    @Test
    void actualSpringDependencyGraphMustStopControllersThenPluginsThenSchemes() {
        var order = new StopOrder();
        var plugins = mock(SpringPluginManager.class);
        doAnswer(ignored -> {
                    order.add("plugins");
                    return null;
                })
                .when(plugins)
                .stopPlugins();
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(StopOrder.class, () -> order);
            context.registerBean(IndexEngine.class, () -> mock(IndexEngine.class));
            context.registerBean(SpringPluginManager.class, () -> plugins);
            context.registerBean(
                    IndexSnapshotManager.class,
                    () -> new IndexSnapshotManager(
                            workDir,
                            mock(ExtensionStoreRepository.class),
                            new IndexSnapshotWriteBarrier(),
                            context.getBean(IndexEngine.class),
                            context.getBeanProvider(SchemeManager.class),
                            context.getBeanProvider(SpringPluginManager.class)));
            context.registerBean(DefaultSchemeManager.class);
            context.registerBean(CoreControllers.class);
            context.registerBean(SchemeInitialization.class);
            context.refresh();
            context.publishEvent(
                    new ApplicationReadyEvent(new SpringApplication(), new String[0], context, Duration.ZERO));
        }
        assertThat(order).containsExactly("controllers", "plugins", "schemes");
    }

    static class StopOrder extends ArrayList<String> {}

    static class CoreControllers implements SmartLifecycle {
        private final List<String> order;
        private boolean running;

        // Mirror the production dependency on the scheme manager. Spring dependencies override phases.
        CoreControllers(SchemeManager schemes, StopOrder order) {
            this.order = order;
        }

        public void start() {
            running = true;
        }

        public void stop() {
            order.add("controllers");
            running = false;
        }

        public boolean isRunning() {
            return running;
        }

        public int getPhase() {
            return InitializationPhase.CONTROLLERS.getPhase();
        }
    }

    static class SchemeInitialization extends CoreControllers {
        private final List<String> order;

        SchemeInitialization(SchemeManager schemes, StopOrder order) {
            super(schemes, order);
            this.order = order;
        }

        @Override
        public void stop() {
            order.add("schemes");
        }

        @Override
        public int getPhase() {
            return InitializationPhase.SCHEME.getPhase();
        }
    }
}
