package run.halo.app.core.extension.reconciler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
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
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.Ref;
import run.halo.app.extension.controller.Reconciler.Request;

@ExtendWith(MockitoExtension.class)
class MenuItemReconcilerTest {

    @Mock
    ExtensionClient client;

    @InjectMocks
    MenuItemReconciler reconciler;

    @Nested
    class WhenCategoryRefSet {

        @Test
        void shouldNotUpdateMenuItemIfCategoryNotFound() {
            Supplier<MenuItem> menuItemSupplier = () -> createMenuItem("fake-name", spec -> {
                spec.setTargetRef(Ref.of("fake-category", Category.GVK));
            });

            when(client.fetch(MenuItem.class, "fake-name"))
                .thenReturn(Optional.of(menuItemSupplier.get()));
            when(client.fetch(Category.class, "fake-category")).thenReturn(Optional.empty());

            var result = reconciler.reconcile(new Request("fake-name"));

            assertTrue(result.reEnqueue());
            assertEquals(Duration.ofMinutes(1), result.retryAfter());
            verify(client).fetch(MenuItem.class, "fake-name");
            verify(client).fetch(Category.class, "fake-category");
            verify(client, never()).update(isA(MenuItem.class));
        }

        @Test
        void shouldUpdateMenuItemIfCategoryFound() {
            Supplier<MenuItem> menuItemSupplier = () -> createMenuItem("fake-name", spec -> {
                spec.setTargetRef(Ref.of("fake-category", Category.GVK));
            });

            when(client.fetch(MenuItem.class, "fake-name"))
                .thenReturn(Optional.of(menuItemSupplier.get()))
                .thenReturn(Optional.of(menuItemSupplier.get()));
            when(client.fetch(Category.class, "fake-category"))
                .thenReturn(Optional.of(createCategory()));

            var result = reconciler.reconcile(new Request("fake-name"));

            assertTrue(result.reEnqueue());
            assertEquals(Duration.ofMinutes(1), result.retryAfter());
            verify(client, times(2)).fetch(MenuItem.class, "fake-name");
            verify(client).fetch(Category.class, "fake-category");
            verify(client).<MenuItem>update(argThat(menuItem -> {
                var status = menuItem.getStatus();
                return status.getHref().equals("fake://permalink")
                    && status.getDisplayName().equals("Fake Category");
            }));
        }

        Category createCategory() {
            var metadata = new Metadata();
            metadata.setName("fake-category");

            var spec = new Category.CategorySpec();
            spec.setDisplayName("Fake Category");
            var status = new Category.CategoryStatus();
            status.setPermalink("fake://permalink");

            var category = new Category();
            category.setMetadata(metadata);
            category.setSpec(spec);
            category.setStatus(status);
            return category;
        }
    }

    @Nested
    class WhenSinglePageRefSet {

        @Test
        void shouldUpdateMenuItemIfPageFound() {
            Supplier<MenuItem> menuItemSupplier = () -> createMenuItem("fake-name",
                spec -> spec.setTargetRef(Ref.of("fake-page", SinglePage.GVK)));

            when(client.fetch(MenuItem.class, "fake-name"))
                .thenReturn(Optional.of(menuItemSupplier.get()))
                .thenReturn(Optional.of(menuItemSupplier.get()));

            when(client.fetch(SinglePage.class, "fake-page"))
                .thenReturn(Optional.of(createSinglePage()));

            var result = reconciler.reconcile(new Request("fake-name"));
            assertTrue(result.reEnqueue());
            assertEquals(Duration.ofMinutes(1), result.retryAfter());
            verify(client, times(2)).fetch(MenuItem.class, "fake-name");
            verify(client).fetch(SinglePage.class, "fake-page");
            verify(client).<MenuItem>update(argThat(menuItem -> {
                var status = menuItem.getStatus();
                return status.getHref().equals("fake://permalink")
                    && status.getDisplayName().equals("fake-title");
            }));
        }

        SinglePage createSinglePage() {
            var metadata = new Metadata();
            metadata.setName("fake-page");

            var spec = new SinglePage.SinglePageSpec();
            spec.setTitle("fake-title");
            var status = new SinglePage.SinglePageStatus();
            status.setPermalink("fake://permalink");

            var singlePage = new SinglePage();
            singlePage.setMetadata(metadata);
            singlePage.setSpec(spec);
            singlePage.setStatus(status);
            return singlePage;
        }
    }

    @Nested
    class WhenOtherRefsNotSet {

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