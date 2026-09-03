package run.halo.app.core.reconciler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.core.extension.MenuItem.MenuItemSpec;
import run.halo.app.core.extension.MenuItem.RouteRef;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.event.post.*;
import run.halo.app.extension.*;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.infra.SystemConfigChangedEvent;

@ExtendWith(MockitoExtension.class)
class MenuItemReconcilerTest {

    @Mock
    ExtensionClient client;

    @Mock
    ReactiveExtensionClient reactiveClient;

    @InjectMocks
    MenuItemReconciler reconciler;

    @Nested
    class WhenRouteRefSet {

        @ParameterizedTest
        @CsvSource({"ARCHIVES,/news", "CATEGORIES,/topics", "TAGS,/labels"})
        void shouldResolveCurrentRouteRules(RouteRef routeRef, String expectedHref) {
            var menuItem = createMenuItem("fake-name", spec -> {
                spec.setDisplayName("Posts");
                spec.setRouteRef(routeRef);
            });
            when(client.fetch(MenuItem.class, "fake-name")).thenReturn(Optional.of(menuItem));
            mockRouteRules("news/", "topics/", "labels/");

            var result = reconciler.reconcile(new Request("fake-name"));

            assertFalse(result.reEnqueue());
            assertEquals(expectedHref, menuItem.getStatus().getHref());
            assertEquals("Posts", menuItem.getStatus().getDisplayName());
            verify(client).update(menuItem);
        }

        @Test
        void shouldUseRouteDefaultsIfConfigMapsAreMissing() {
            var menuItem = createMenuItem("fake-name", spec -> {
                spec.setDisplayName("Tags");
                spec.setRouteRef(RouteRef.TAGS);
            });
            when(client.fetch(MenuItem.class, "fake-name")).thenReturn(Optional.of(menuItem));

            reconciler.reconcile(new Request("fake-name"));

            assertEquals("/tags", menuItem.getStatus().getHref());
            assertEquals("Tags", menuItem.getStatus().getDisplayName());
        }

        @Test
        void shouldResetStatusIfRouteAndResourceRefsConflict() {
            var menuItem = createMenuItem("fake-name", spec -> {
                spec.setDisplayName("Posts");
                spec.setRouteRef(RouteRef.ARCHIVES);
                spec.setTargetRef(Ref.of("fake-post", Post.GVK));
            });
            menuItem.setStatus(status("/old", "Old"));
            when(client.fetch(MenuItem.class, "fake-name")).thenReturn(Optional.of(menuItem));

            reconciler.reconcile(new Request("fake-name"));

            assertNull(menuItem.getStatus().getHref());
            assertNull(menuItem.getStatus().getDisplayName());
            verify(client).update(menuItem);
        }

        @Test
        void shouldResetStatusIfDisplayNameIsMissing() {
            var menuItem = createMenuItem("fake-name", spec -> spec.setRouteRef(RouteRef.ARCHIVES));
            menuItem.setStatus(status("/old", "Old"));
            when(client.fetch(MenuItem.class, "fake-name")).thenReturn(Optional.of(menuItem));

            reconciler.reconcile(new Request("fake-name"));

            assertNull(menuItem.getStatus().getHref());
            assertNull(menuItem.getStatus().getDisplayName());
            verify(client).update(menuItem);
        }

        @Test
        void shouldPreserveStatusIfRouteRuleCannotBeNormalized() {
            var menuItem = createMenuItem("fake-name", spec -> {
                spec.setDisplayName("Posts");
                spec.setRouteRef(RouteRef.ARCHIVES);
            });
            menuItem.setStatus(status("/old", "Old"));
            when(client.fetch(MenuItem.class, "fake-name")).thenReturn(Optional.of(menuItem));
            mockRouteRules("/", "topics", "labels");

            reconciler.reconcile(new Request("fake-name"));

            assertEquals("/old", menuItem.getStatus().getHref());
            assertEquals("Old", menuItem.getStatus().getDisplayName());
            verify(client, never()).update(menuItem);
        }
    }

    @Nested
    class WhenRouteRulesChanged {

        @Test
        void shouldRequestUpdatesOnlyForMatchingRouteRefs() {
            var archives = createMenuItem("archives", spec -> spec.setRouteRef(RouteRef.ARCHIVES));
            var tags = createMenuItem("tags", spec -> spec.setRouteRef(RouteRef.TAGS));
            var custom = createMenuItem("custom", spec -> spec.setHref("/custom"));
            when(reactiveClient.listAll(same(MenuItem.class), isA(ListOptions.class), eq(Sort.unsorted())))
                    .thenReturn(Flux.just(archives, tags, custom));
            when(reactiveClient.update(archives)).thenReturn(Mono.just(archives));

            reconciler
                    .onSystemConfigChanged(routeRulesChanged("old", "categories", "tags", "new", "categories", "tags"))
                    .as(StepVerifier::create)
                    .verifyComplete();

            assertNotNull(archives.getMetadata().getAnnotations());
            assertTrue(archives.getMetadata().getAnnotations().containsKey(MenuItem.REQUEST_TO_UPDATE_ANNO));
            assertNull(tags.getMetadata().getAnnotations());
            assertNull(custom.getMetadata().getAnnotations());
            verify(reactiveClient).update(archives);
            verify(reactiveClient, never()).update(tags);
            verify(reactiveClient, never()).update(custom);
        }

        @Test
        void shouldContinueRequestingUpdatesAfterOneFailure() {
            var first = createMenuItem("first", spec -> spec.setRouteRef(RouteRef.ARCHIVES));
            var second = createMenuItem("second", spec -> spec.setRouteRef(RouteRef.ARCHIVES));
            when(reactiveClient.listAll(same(MenuItem.class), isA(ListOptions.class), eq(Sort.unsorted())))
                    .thenReturn(Flux.just(first, second));
            when(reactiveClient.update(first)).thenReturn(Mono.error(new IllegalStateException("failed")));
            when(reactiveClient.update(second)).thenReturn(Mono.just(second));

            reconciler
                    .onSystemConfigChanged(routeRulesChanged("old", "categories", "tags", "new", "categories", "tags"))
                    .as(StepVerifier::create)
                    .verifyComplete();

            verify(reactiveClient).update(first);
            verify(reactiveClient).update(second);
        }
    }

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

    void mockRouteRules(String archives, String categories, String tags) {
        var defaults = new ConfigMap();
        defaults.setData(Map.of("routeRules", """
                {"archives":"archives","categories":"categories","tags":"tags"}
                """));
        var current = new ConfigMap();
        current.setData(Map.of("routeRules", """
                {"archives":"%s","categories":"%s","tags":"%s"}
                """.formatted(archives, categories, tags)));
        when(client.fetch(ConfigMap.class, "system-default")).thenReturn(Optional.of(defaults));
        when(client.fetch(ConfigMap.class, "system")).thenReturn(Optional.of(current));
    }

    MenuItem.MenuItemStatus status(String href, String displayName) {
        var status = new MenuItem.MenuItemStatus();
        status.setHref(href);
        status.setDisplayName(displayName);
        return status;
    }

    SystemConfigChangedEvent routeRulesChanged(
            String oldArchives,
            String oldCategories,
            String oldTags,
            String newArchives,
            String newCategories,
            String newTags) {
        return new SystemConfigChangedEvent(
                this,
                Map.of("routeRules", """
                        {"archives":"%s","categories":"%s","tags":"%s"}
                        """.formatted(oldArchives, oldCategories, oldTags)),
                Map.of("routeRules", """
                        {"archives":"%s","categories":"%s","tags":"%s"}
                        """.formatted(newArchives, newCategories, newTags)));
    }
}
