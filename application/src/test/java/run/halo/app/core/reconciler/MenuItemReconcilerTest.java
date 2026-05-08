package run.halo.app.core.reconciler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.core.extension.MenuItem.MenuItemSpec;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.event.post.*;
import run.halo.app.extension.*;
import run.halo.app.extension.controller.Reconciler.Request;

@ExtendWith(MockitoExtension.class)
class MenuItemReconcilerTest {

    @Mock
    ExtensionClient client;

    @Mock
    ReactiveExtensionClient reactiveClient;

    @InjectMocks
    MenuItemReconciler reconciler;

    @Nested
    class WhenCategoryRefSet {

        @Test
        void shouldResetMenuItemIfCategoryNotFound() {
            var menuItem =
                    createMenuItem("fake-name", spec -> spec.setTargetRef(Ref.of("fake-category", Category.GVK)));
            var status = new MenuItem.MenuItemStatus();
            menuItem.setStatus(status);
            status.setHref("fake://old-permalink");
            status.setDisplayName("Old display name");

            when(client.fetch(MenuItem.class, "fake-name")).thenReturn(Optional.of(menuItem));
            when(client.fetch(Category.class, "fake-category")).thenReturn(Optional.empty());

            var result = reconciler.reconcile(new Request("fake-name"));

            assertFalse(result.reEnqueue());
            assertNotNull(menuItem.getStatus());
            assertNull(menuItem.getStatus().getHref());
            assertNull(menuItem.getStatus().getDisplayName());
            verify(client).update(menuItem);
        }

        @Test
        void shouldUpdateMenuItemIfCategoryFound() {
            var menuItem = createMenuItem("fake-name", spec -> {
                spec.setTargetRef(Ref.of("fake-category", Category.GVK));
            });

            when(client.fetch(MenuItem.class, "fake-name")).thenReturn(Optional.of(menuItem));
            when(client.fetch(Category.class, "fake-category")).thenReturn(Optional.of(createCategory()));

            var result = reconciler.reconcile(new Request("fake-name"));

            assertFalse(result.reEnqueue());
            assertNotNull(menuItem.getStatus());
            assertEquals("fake://permalink", menuItem.getStatus().getHref());
            assertEquals("Fake Category", menuItem.getStatus().getDisplayName());
            verify(client).update(menuItem);
        }

        @Test
        void shouldRequestToUpdateWhenCategoryUpdated() {
            var event = new CategoryUpdatedEvent(this, createCategory());

            var menuItem = createMenuItem("fake-name", spec -> {});

            when(reactiveClient.listAll(same(MenuItem.class), isA(ListOptions.class), eq(Sort.unsorted())))
                    .thenReturn(Flux.just(menuItem));
            when(reactiveClient.update(menuItem)).thenReturn(Mono.just(menuItem));

            reconciler.onCategoryUpdated(event).as(StepVerifier::create).verifyComplete();

            var annotations = menuItem.getMetadata().getAnnotations();
            assertNotNull(annotations);
            assertTrue(annotations.containsKey(MenuItem.REQUEST_TO_UPDATE_ANNO));
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
            var menuItem = createMenuItem("fake-name", spec -> spec.setTargetRef(Ref.of("fake-page", SinglePage.GVK)));

            when(client.fetch(MenuItem.class, "fake-name")).thenReturn(Optional.of(menuItem));
            when(client.fetch(SinglePage.class, "fake-page")).thenReturn(Optional.of(createSinglePage()));

            var result = reconciler.reconcile(new Request("fake-name"));

            assertFalse(result.reEnqueue());

            assertNotNull(menuItem.getStatus());
            assertEquals("fake://permalink", menuItem.getStatus().getHref());
            assertEquals("fake-title", menuItem.getStatus().getDisplayName());
            verify(client).update(menuItem);
        }

        @Test
        void shouldRequestToUpdateWhenSinglePageUpdated() {
            var event = new SinglePageUpdatedEvent(this, createSinglePage());
            var menuItem = createMenuItem("fake-name", spec -> {});

            when(reactiveClient.listAll(same(MenuItem.class), isA(ListOptions.class), eq(Sort.unsorted())))
                    .thenReturn(Flux.just(menuItem));
            when(reactiveClient.update(menuItem)).thenReturn(Mono.just(menuItem));

            reconciler.onSinglePageUpdated(event).as(StepVerifier::create).verifyComplete();
            var annotations = menuItem.getMetadata().getAnnotations();
            assertNotNull(annotations);
            assertTrue(annotations.containsKey(MenuItem.REQUEST_TO_UPDATE_ANNO));
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
    class WhenPostRefSet {

        @Test
        void shouldResetMenuItemIfPostNotFound() {
            var menuItem = createMenuItem("fake-name", spec -> spec.setTargetRef(Ref.of("fake-post", Post.GVK)));
            var status = new MenuItem.MenuItemStatus();
            menuItem.setStatus(status);
            status.setHref("fake://old-permalink");
            status.setDisplayName("Old display name");

            when(client.fetch(MenuItem.class, "fake-name")).thenReturn(Optional.of(menuItem));
            when(client.fetch(Post.class, "fake-post")).thenReturn(Optional.empty());

            var result = reconciler.reconcile(new Request("fake-name"));

            assertFalse(result.reEnqueue());
            assertNotNull(menuItem.getStatus());
            assertNull(menuItem.getStatus().getHref());
            assertNull(menuItem.getStatus().getDisplayName());
            verify(client).update(menuItem);
        }

        @Test
        void shouldUpdateMenuItemIfPostFound() {
            var menuItem = createMenuItem("fake-name", spec -> {
                spec.setTargetRef(Ref.of("fake-post", Post.GVK));
            });

            when(client.fetch(MenuItem.class, "fake-name")).thenReturn(Optional.of(menuItem));
            when(client.fetch(Post.class, "fake-post")).thenReturn(Optional.of(createPost()));

            var result = reconciler.reconcile(new Request("fake-name"));

            assertFalse(result.reEnqueue());
            assertNotNull(menuItem.getStatus());
            assertEquals("fake://permalink", menuItem.getStatus().getHref());
            assertEquals("Fake Post", menuItem.getStatus().getDisplayName());
            verify(client).update(menuItem);
        }

        @Test
        void shouldRequestToUpdateWhenPostUpdated() {
            var event = new PostUpdatedEvent(this, "fake-post");

            var menuItem = createMenuItem("fake-name", spec -> {});

            when(reactiveClient.listAll(same(MenuItem.class), isA(ListOptions.class), eq(Sort.unsorted())))
                    .thenReturn(Flux.just(menuItem));
            when(reactiveClient.update(menuItem)).thenReturn(Mono.just(menuItem));

            reconciler.onPostUpdated(event).as(StepVerifier::create).verifyComplete();

            var annotations = menuItem.getMetadata().getAnnotations();
            assertNotNull(annotations);
            assertTrue(annotations.containsKey(MenuItem.REQUEST_TO_UPDATE_ANNO));
        }

        @Test
        void shouldRequestToUpdateWhenPostDeleted() {
            var post = createPost();
            post.getMetadata().setDeletionTimestamp(Instant.now());
            var event = new PostDeletedEvent(this, post);

            var menuItem = createMenuItem("fake-name", spec -> {});

            when(reactiveClient.listAll(same(MenuItem.class), isA(ListOptions.class), eq(Sort.unsorted())))
                    .thenReturn(Flux.just(menuItem));
            when(reactiveClient.update(menuItem)).thenReturn(Mono.just(menuItem));

            reconciler.onPostDeleted(event).as(StepVerifier::create).verifyComplete();

            var annotations = menuItem.getMetadata().getAnnotations();
            assertNotNull(annotations);
            assertTrue(annotations.containsKey(MenuItem.REQUEST_TO_UPDATE_ANNO));
        }

        Post createPost() {
            var metadata = new Metadata();
            metadata.setName("fake-post");

            var spec = new Post.PostSpec();
            spec.setTitle("Fake Post");
            var status = new Post.PostStatus();
            status.setPermalink("fake://permalink");

            var post = new Post();
            post.setMetadata(metadata);
            post.setSpec(spec);
            post.setStatus(status);
            return post;
        }
    }

    @Nested
    class WhenTagRefSet {

        @Test
        void shouldUpdateMenuItemIfTagFound() {
            var menuItem = createMenuItem("fake-name", spec -> {
                spec.setTargetRef(Ref.of("fake-tag", Tag.GVK));
            });

            when(client.fetch(MenuItem.class, "fake-name")).thenReturn(Optional.of(menuItem));
            when(client.fetch(Tag.class, "fake-tag")).thenReturn(Optional.of(createTag()));

            var result = reconciler.reconcile(new Request("fake-name"));

            assertFalse(result.reEnqueue());
            assertNotNull(menuItem.getStatus());
            assertEquals("fake://permalink", menuItem.getStatus().getHref());
            assertEquals("Fake Tag", menuItem.getStatus().getDisplayName());
            verify(client).update(menuItem);
        }

        @Test
        void shouldResetMenuItemIfTagNotFound() {
            var menuItem = createMenuItem("fake-name", spec -> spec.setTargetRef(Ref.of("fake-tag", Tag.GVK)));
            var status = new MenuItem.MenuItemStatus();
            menuItem.setStatus(status);
            status.setHref("fake://old-permalink");
            status.setDisplayName("Old display name");

            when(client.fetch(MenuItem.class, "fake-name")).thenReturn(Optional.of(menuItem));
            when(client.fetch(Tag.class, "fake-tag")).thenReturn(Optional.empty());

            var result = reconciler.reconcile(new Request("fake-name"));

            assertFalse(result.reEnqueue());
            assertNotNull(menuItem.getStatus());
            assertNull(menuItem.getStatus().getHref());
            assertNull(menuItem.getStatus().getDisplayName());
            verify(client).update(menuItem);
        }

        @Test
        void shouldRequestToUpdateWhenTagUpdated() {
            var tag = createTag();
            var event = new TagUpdatedEvent(this, tag);

            var menuItem = createMenuItem("fake-name", spec -> {});

            when(reactiveClient.listAll(same(MenuItem.class), isA(ListOptions.class), eq(Sort.unsorted())))
                    .thenReturn(Flux.just(menuItem));
            when(reactiveClient.update(menuItem)).thenReturn(Mono.just(menuItem));

            reconciler.onTagUpdated(event).as(StepVerifier::create).verifyComplete();
            var annotations = menuItem.getMetadata().getAnnotations();
            assertNotNull(annotations);
            assertTrue(annotations.containsKey(MenuItem.REQUEST_TO_UPDATE_ANNO));
        }

        Tag createTag() {
            var metadata = new Metadata();
            metadata.setName("fake-tag");

            var spec = new Tag.TagSpec();
            spec.setDisplayName("Fake Tag");
            var status = new Tag.TagStatus();
            status.setPermalink("fake://permalink");

            var tag = new Tag();
            tag.setMetadata(metadata);
            tag.setSpec(spec);
            tag.setStatus(status);
            return tag;
        }
    }

    @Nested
    class WhenOtherRefsNotSet {

        @Test
        void shouldResetIfRefNotSupported() {
            var menuItem = createMenuItem(
                    "fake-name",
                    spec -> spec.setTargetRef(
                            Ref.of("fake-ref", GroupVersionKind.fromAPIVersionAndKind("fake.group/v1", "FakeKind"))));
            var status = new MenuItem.MenuItemStatus();
            menuItem.setStatus(status);
            status.setHref("fake://old-permalink");
            status.setDisplayName("Old display name");

            when(client.fetch(MenuItem.class, "fake-name")).thenReturn(Optional.of(menuItem));

            var result = reconciler.reconcile(new Request("fake-name"));

            assertFalse(result.reEnqueue());
            assertNotNull(menuItem.getStatus());
            assertNull(menuItem.getStatus().getHref());
            assertNull(menuItem.getStatus().getDisplayName());
            verify(client).update(menuItem);
        }

        @Test
        void shouldResetIfHrefNotSet() {
            var menuItem = createMenuItem("fake-name", spec -> spec.setDisplayName("Fake display name"));
            when(client.fetch(MenuItem.class, "fake-name")).thenReturn(Optional.of(menuItem));

            var result = reconciler.reconcile(new Request("fake-name"));

            assertFalse(result.reEnqueue());
            assertNotNull(menuItem.getStatus());
            assertNull(menuItem.getStatus().getHref());
            assertEquals("Fake display name", menuItem.getStatus().getDisplayName());
            verify(client).update(menuItem);
        }

        @Test
        void shouldUpdateIfDisplayNameNotSet() {
            var menuItem = createMenuItem("fake-name", spec -> {
                spec.setHref("/fake");
            });
            when(client.fetch(MenuItem.class, "fake-name")).thenReturn(Optional.of(menuItem));

            var result = reconciler.reconcile(new Request("fake-name"));

            assertFalse(result.reEnqueue());
            assertNotNull(menuItem.getStatus());
            assertEquals("/fake", menuItem.getStatus().getHref());
            assertNull(menuItem.getStatus().getDisplayName());
            verify(client).update(menuItem);
        }

        @Test
        void shouldUpdateIfHrefAndDisplayNameSet() {
            var menuItem = createMenuItem("fake-name", spec -> {
                spec.setHref("/fake");
                spec.setDisplayName("Fake display name");
            });

            when(client.fetch(MenuItem.class, "fake-name")).thenReturn(Optional.of(menuItem));

            var result = reconciler.reconcile(new Request("fake-name"));

            assertFalse(result.reEnqueue());
            assertNotNull(menuItem.getStatus());
            assertEquals("/fake", menuItem.getStatus().getHref());
            assertEquals("Fake display name", menuItem.getStatus().getDisplayName());
            verify(client).update(menuItem);
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
