package run.halo.app.core.extension.reconciler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.core.extension.MenuItem.MenuItemSpec;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler.Request;

@ExtendWith(MockitoExtension.class)
class MenuItemReconcilerTest {

    @Mock
    ExtensionClient client;

    @InjectMocks
    MenuItemReconciler reconciler;

    @Nested
    class WhenOtherRefNotSet {


        @Test
        void shouldNotRequeueIfHrefNotSet() {
            var menuItem = createMenuItem("fake-name", spec -> {
                spec.setHref(null);
                spec.setDisplayName("Fake display name");
            });
            when(client.fetch(MenuItem.class, "fake-name")).thenReturn(Optional.of(menuItem));

            var result = reconciler.reconcile(new Request("fake-name"));
            assertFalse(result.reEnqueue());

            verify(client).fetch(MenuItem.class, "fake-name");
            verify(client, never()).update(menuItem);
        }

        @Test
        void shouldNotRequeueIfDisplayNameNotSet() {
            var menuItem = createMenuItem("fake-name", spec -> {
                spec.setHref("/fake");
                spec.setDisplayName(null);
            });
            when(client.fetch(MenuItem.class, "fake-name")).thenReturn(Optional.of(menuItem));
            var result = reconciler.reconcile(new Request("fake-name"));
            assertFalse(result.reEnqueue());

            verify(client).fetch(MenuItem.class, "fake-name");
            verify(client, never()).update(menuItem);
        }

        @Test
        void shouldReconcileIfHrefAndDisplayNameSet() {
            Supplier<MenuItem> menuItemSupplier = () -> createMenuItem("fake-name", spec -> {
                spec.setHref("/fake");
                spec.setDisplayName("Fake display name");
            });

            when(client.fetch(MenuItem.class, "fake-name"))
                .thenReturn(Optional.of(menuItemSupplier.get()))
                .thenReturn(Optional.of(menuItemSupplier.get()));

            var result = reconciler.reconcile(new Request("fake-name"));
            assertFalse(result.reEnqueue());

            verify(client, times(2)).fetch(MenuItem.class, "fake-name");
            verify(client).update(argThat(ext -> {
                if (!(ext instanceof MenuItem menuItem)) {
                    return false;
                }
                return menuItem.getStatus().getHref().equals("/fake")
                    && menuItem.getStatus().getDisplayName().equals("Fake display name");
            }));
        }
    }

    MenuItem createMenuItem(String name, Consumer<MenuItemSpec> specCustomizer) {
        var metadata = new Metadata();
        metadata.setName(name);
        var menuItem = new MenuItem();
        menuItem.setMetadata(metadata);
        var spec = new MenuItemSpec();
        if (specCustomizer != null) {
            specCustomizer.accept(spec);
        }
        menuItem.setSpec(spec);
        return menuItem;
    }
}