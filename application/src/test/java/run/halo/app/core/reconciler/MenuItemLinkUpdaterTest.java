package run.halo.app.core.reconciler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.core.extension.MenuItem.MenuItemSpec;
import run.halo.app.core.extension.MenuItem.MenuItemStatus;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.Ref;
import run.halo.app.extension.Watcher;

/**
 * Tests for {@link MenuItemLinkUpdater}.
 *
 * @author johnniang
 */
@ExtendWith(MockitoExtension.class)
class MenuItemLinkUpdaterTest {

    @Mock
    ExtensionClient client;

    @InjectMocks
    MenuItemLinkUpdater linkUpdater;

    @Nested
    class LifecycleTest {

        @Test
        void shouldRegisterWatcherOnStart() {
            linkUpdater.start();
            verify(client).watch(any(Watcher.class));
            assertThat(linkUpdater.isRunning()).isTrue();
        }

        @Test
        void shouldDisposeWatcherOnStop() {
            linkUpdater.start();
            linkUpdater.stop();
            assertThat(linkUpdater.isRunning()).isFalse();
        }
    }

    @Nested
    class WhenPostUpdated {

        @Test
        void shouldUpdateMenuItemStatusWhenPostHasPermalink() {
            var post = createPost("fake-post", "/posts/fake");
            var menuItem = createMenuItem("fake-menu-item",
                spec -> spec.setTargetRef(Ref.of("fake-post", Post.GVK)));
            menuItem.setStatus(new MenuItemStatus());

            when(client.listAll(eq(MenuItem.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(List.of(menuItem));

            linkUpdater.reconcileMenuItems(post);

            verify(client).update(argThat(ext -> {
                if (!(ext instanceof MenuItem mi)) {
                    return false;
                }
                return "/posts/fake".equals(mi.getStatus().getHref())
                    && "Fake Post".equals(mi.getStatus().getDisplayName());
            }));
        }

        @Test
        void shouldNotUpdateMenuItemWhenStatusUnchanged() {
            var menuItem = createMenuItem("fake-menu-item",
                spec -> spec.setTargetRef(Ref.of("fake-post", Post.GVK)));
            var existingStatus = new MenuItemStatus();
            existingStatus.setHref("/posts/fake");
            existingStatus.setDisplayName("Fake Post");
            menuItem.setStatus(existingStatus);

            when(client.listAll(eq(MenuItem.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(List.of(menuItem));

            var post = createPost("fake-post", "/posts/fake");
            linkUpdater.reconcileMenuItems(post);

            verify(client, never()).update(any());
        }

        @Test
        void shouldNotUpdateMenuItemWhenPostHasNoPermalink() {
            var post = createPost("fake-post", null);
            var menuItem = createMenuItem("fake-menu-item",
                spec -> spec.setTargetRef(Ref.of("fake-post", Post.GVK)));

            when(client.listAll(eq(MenuItem.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(List.of(menuItem));

            linkUpdater.reconcileMenuItems(post);

            verify(client, never()).update(any());
        }

        @Test
        void shouldSkipMenuItemsWithDifferentTargetRefKind() {
            var post = createPost("fake-name", "/posts/fake");
            // MenuItem references a Category named "fake-name", not a Post
            var menuItem = createMenuItem("fake-menu-item",
                spec -> spec.setTargetRef(Ref.of("fake-name", Category.GVK)));

            when(client.listAll(eq(MenuItem.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(List.of(menuItem));

            linkUpdater.reconcileMenuItems(post);

            verify(client, never()).update(any());
        }

        Post createPost(String name, String permalink) {
            var metadata = new Metadata();
            metadata.setName(name);
            var spec = new Post.PostSpec();
            spec.setTitle("Fake Post");
            var status = new Post.PostStatus();
            status.setPermalink(permalink);
            var post = new Post();
            post.setMetadata(metadata);
            post.setSpec(spec);
            post.setStatus(status);
            return post;
        }
    }

    @Nested
    class WhenCategoryUpdated {

        @Test
        void shouldUpdateMenuItemStatusWhenCategoryHasPermalink() {
            var category = createCategory("fake-cat", "/categories/fake");
            var menuItem = createMenuItem("fake-menu-item",
                spec -> spec.setTargetRef(Ref.of("fake-cat", Category.GVK)));
            menuItem.setStatus(new MenuItemStatus());

            when(client.listAll(eq(MenuItem.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(List.of(menuItem));

            linkUpdater.reconcileMenuItems(category);

            verify(client).update(argThat(ext -> {
                if (!(ext instanceof MenuItem mi)) {
                    return false;
                }
                return "/categories/fake".equals(mi.getStatus().getHref())
                    && "Fake Category".equals(mi.getStatus().getDisplayName());
            }));
        }

        Category createCategory(String name, String permalink) {
            var metadata = new Metadata();
            metadata.setName(name);
            var spec = new Category.CategorySpec();
            spec.setDisplayName("Fake Category");
            var status = new Category.CategoryStatus();
            status.setPermalink(permalink);
            var category = new Category();
            category.setMetadata(metadata);
            category.setSpec(spec);
            category.setStatus(status);
            return category;
        }
    }

    @Nested
    class WhenTagUpdated {

        @Test
        void shouldUpdateMenuItemStatusWhenTagHasPermalink() {
            var tag = createTag("fake-tag", "/tags/fake");
            var menuItem = createMenuItem("fake-menu-item",
                spec -> spec.setTargetRef(Ref.of("fake-tag", Tag.GVK)));
            menuItem.setStatus(new MenuItemStatus());

            when(client.listAll(eq(MenuItem.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(List.of(menuItem));

            linkUpdater.reconcileMenuItems(tag);

            verify(client).update(argThat(ext -> {
                if (!(ext instanceof MenuItem mi)) {
                    return false;
                }
                return "/tags/fake".equals(mi.getStatus().getHref())
                    && "Fake Tag".equals(mi.getStatus().getDisplayName());
            }));
        }

        Tag createTag(String name, String permalink) {
            var metadata = new Metadata();
            metadata.setName(name);
            var spec = new Tag.TagSpec();
            spec.setDisplayName("Fake Tag");
            var status = new Tag.TagStatus();
            status.setPermalink(permalink);
            var tag = new Tag();
            tag.setMetadata(metadata);
            tag.setSpec(spec);
            tag.setStatus(status);
            return tag;
        }
    }

    @Nested
    class WhenSinglePageUpdated {

        @Test
        void shouldUpdateMenuItemStatusWhenSinglePageHasPermalink() {
            var page = createSinglePage("fake-page", "/pages/fake");
            var menuItem = createMenuItem("fake-menu-item",
                spec -> spec.setTargetRef(Ref.of("fake-page", SinglePage.GVK)));
            menuItem.setStatus(new MenuItemStatus());

            when(client.listAll(eq(MenuItem.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(List.of(menuItem));

            linkUpdater.reconcileMenuItems(page);

            verify(client).update(argThat(ext -> {
                if (!(ext instanceof MenuItem mi)) {
                    return false;
                }
                return "/pages/fake".equals(mi.getStatus().getHref())
                    && "Fake Page".equals(mi.getStatus().getDisplayName());
            }));
        }

        SinglePage createSinglePage(String name, String permalink) {
            var metadata = new Metadata();
            metadata.setName(name);
            var spec = new SinglePage.SinglePageSpec();
            spec.setTitle("Fake Page");
            var status = new SinglePage.SinglePageStatus();
            status.setPermalink(permalink);
            var page = new SinglePage();
            page.setMetadata(metadata);
            page.setSpec(spec);
            page.setStatus(status);
            return page;
        }
    }

    @Nested
    class LinkedResourceWatcherTest {

        @Test
        void shouldIgnoreNonWatchedExtensions() {
            linkUpdater.start();

            var captor = ArgumentCaptor.forClass(Watcher.class);
            verify(client).watch(captor.capture());
            var watcher = captor.getValue();

            // Non-watched type: Menu
            var menu = new run.halo.app.core.extension.Menu();
            menu.setMetadata(new Metadata());
            menu.getMetadata().setName("fake-menu");

            watcher.onUpdate(menu, menu);

            verify(client, times(0)).listAll(any(), any(), any());
        }

        @Test
        void shouldForwardPostUpdateToReconcileMenuItems() {
            linkUpdater.start();

            var captor = ArgumentCaptor.forClass(Watcher.class);
            verify(client).watch(captor.capture());
            var watcher = captor.getValue();

            var post = createPost("fake-post", "/posts/fake");
            when(client.listAll(eq(MenuItem.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(List.of());

            watcher.onUpdate(post, post);

            verify(client, times(1)).listAll(eq(MenuItem.class), any(), any());
        }

        @Test
        void shouldNotProcessEventsAfterDisposal() {
            linkUpdater.start();

            var captor = ArgumentCaptor.forClass(Watcher.class);
            verify(client).watch(captor.capture());
            var watcher = captor.getValue();

            watcher.dispose();

            var post = createPost("fake-post", "/posts/fake");
            watcher.onUpdate(post, post);

            verify(client, never()).listAll(any(), any(), any());
        }

        @Test
        void shouldForwardPostAddToReconcileMenuItems() {
            linkUpdater.start();

            var captor = ArgumentCaptor.forClass(Watcher.class);
            verify(client).watch(captor.capture());
            var watcher = captor.getValue();

            var post = createPost("fake-post", "/posts/fake");
            when(client.listAll(eq(MenuItem.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(List.of());

            watcher.onAdd(post);

            verify(client, times(1)).listAll(eq(MenuItem.class), any(), any());
        }

        Post createPost(String name, String permalink) {
            var metadata = new Metadata();
            metadata.setName(name);
            var spec = new Post.PostSpec();
            spec.setTitle("Fake Post");
            var status = new Post.PostStatus();
            status.setPermalink(permalink);
            var post = new Post();
            post.setMetadata(metadata);
            post.setSpec(spec);
            post.setStatus(status);
            return post;
        }
    }

    MenuItem createMenuItem(String name,
        java.util.function.Consumer<MenuItemSpec> specCustomizer) {
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
