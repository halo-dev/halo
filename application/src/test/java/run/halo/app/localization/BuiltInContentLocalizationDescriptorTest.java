package run.halo.app.localization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import run.halo.app.content.ContentWrapper;
import run.halo.app.content.PostService;
import run.halo.app.content.SinglePageService;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.Metadata;

class BuiltInContentLocalizationDescriptorTest {
    private static final String PREFERRED_EDITOR = "content.halo.run/preferred-editor";

    @Test
    void postReleaseContentSuppliesDocumentsAndMetadataFields() {
        var service = mock(PostService.class);
        var descriptor = new PostLocalizationDescriptor(service);
        var post = post();
        when(service.getReleaseContent(post))
                .thenReturn(Mono.just(ContentWrapper.builder()
                        .raw("# Source")
                        .content("<h1>Source</h1>")
                        .rawType("markdown")
                        .build()));

        var documents = descriptor.resolveDocuments(post).block();
        var fields = descriptor
                .resolveFields(post, Map.of("title", "Source title", "content.html", "stale", "content.raw", "stale"))
                .block();

        assertThat(documents)
                .containsExactly(new LocalizableDocument("content.html", "content.raw", "markdown", "bytemd"));
        assertThat(fields)
                .containsEntry("title", "Source title")
                .containsEntry("content.html", "<h1>Source</h1>")
                .containsEntry("content.raw", "# Source");
        assertThat(post.getSpec().getTitle()).isEqualTo("Source title");
        verify(service, times(2)).getReleaseContent(post);
    }

    @Test
    void postWithoutReleasedContentKeepsOnlyMetadataFields() {
        var service = mock(PostService.class);
        var descriptor = new PostLocalizationDescriptor(service);
        var post = post();
        when(service.getReleaseContent(post)).thenReturn(Mono.empty());

        assertThat(descriptor.resolveDocuments(post).block()).isEmpty();
        assertThat(descriptor
                        .resolveFields(post, Map.of("title", "Source", "content.html", "<p>Stale</p>"))
                        .block())
                .containsOnlyKeys("title");
    }

    @Test
    void postContentReadFailuresDoNotHideMetadataFields() {
        var service = mock(PostService.class);
        var descriptor = new PostLocalizationDescriptor(service);
        var post = post();
        when(service.getReleaseContent(post)).thenReturn(Mono.error(new IllegalStateException("unavailable")));

        assertThat(descriptor.resolveDocuments(post).block()).isEmpty();
        assertThat(descriptor.resolveFields(post, Map.of("title", "Source")).block())
                .containsExactlyEntriesOf(Map.of("title", "Source"));
    }

    @Test
    void singlePageReleaseContentUsesTheSameGenericDocumentBinding() {
        var service = mock(SinglePageService.class);
        var descriptor = new SinglePageLocalizationDescriptor(service);
        var page = page();
        when(service.getReleaseContent("page"))
                .thenReturn(Mono.just(ContentWrapper.builder()
                        .raw("<p>Source</p>")
                        .content("<p>Source</p>")
                        .rawType("HTML")
                        .build()));

        assertThat(descriptor.resolveDocuments(page).block())
                .containsExactly(new LocalizableDocument("content.html", "content.raw", "HTML", null));
        assertThat(descriptor
                        .resolveFields(page, Map.of("title", "Source title"))
                        .block())
                .containsEntry("content.raw", "<p>Source</p>")
                .containsEntry("content.html", "<p>Source</p>");
        verify(service, times(2)).getReleaseContent("page");
    }

    @Test
    void publicAndIndexableDecisionsRemainResourceOwned() {
        var postDescriptor = new PostLocalizationDescriptor();
        var post = post();
        assertThat(postDescriptor.publiclyVisible(post)).isTrue();
        assertThat(postDescriptor.indexable(post)).isTrue();
        post.getSpec().setHtmlMetas(List.of(Map.of("name", "robots", "content", "noindex, nofollow")));
        assertThat(postDescriptor.indexable(post)).isFalse();
        post.getSpec().setHtmlMetas(List.of());
        post.getSpec().setVisible(Post.VisibleEnum.PRIVATE);
        assertThat(postDescriptor.publiclyVisible(post)).isFalse();

        var pageDescriptor = new SinglePageLocalizationDescriptor();
        var page = page();
        assertThat(pageDescriptor.publiclyVisible(page)).isTrue();
        page.getSpec().setVisible(Post.VisibleEnum.PRIVATE);
        assertThat(pageDescriptor.publiclyVisible(page)).isFalse();
    }

    @Test
    void builtInDescriptorsExposeSourceRevisionPermalinkAndLinks() {
        var post = post();
        post.getStatus().setPermalink("/archives/source");
        post.getStatus().setLastModifyTime(Instant.parse("2026-01-02T03:04:05Z"));
        var descriptor = new PostLocalizationDescriptor();

        assertThat(descriptor.linkPointers()).containsExactly("/status/permalink");
        assertThat(descriptor.revision(post)).isEqualTo("release");
        assertThat(descriptor.permalink(post)).isEqualTo("/archives/source");
        assertThat(descriptor.updatedAt(post)).isEqualTo(Instant.parse("2026-01-02T03:04:05Z"));
    }

    @Test
    void builtInTaxonomyAndAuthorDescriptorsExposeOnlyPublicPages() {
        var tagDescriptor = new TagLocalizationDescriptor();
        var tag = new Tag();
        tag.setMetadata(metadata("tag"));
        tag.setSpec(new Tag.TagSpec());
        tag.setStatus(new Tag.TagStatus());
        assertThat(tagDescriptor.publiclyVisible(tag)).isFalse();
        tag.getStatus().setPermalink("/tags/source");
        assertThat(tagDescriptor.publiclyVisible(tag)).isTrue();
        assertThat(tagDescriptor.indexable(tag)).isTrue();
        assertThat(tagDescriptor.permalink(tag)).isEqualTo("/tags/source");
        assertThat(tagDescriptor.revision(tag)).isEmpty();

        var categoryDescriptor = new CategoryLocalizationDescriptor();
        var category = new Category();
        category.setMetadata(metadata("category"));
        category.setSpec(new Category.CategorySpec());
        category.setStatus(new Category.CategoryStatus());
        category.getStatus().setPermalink("/categories/source");
        assertThat(categoryDescriptor.publiclyVisible(category)).isTrue();
        assertThat(categoryDescriptor.permalink(category)).isEqualTo("/categories/source");

        var userDescriptor = new UserLocalizationDescriptor();
        var user = new User();
        user.setMetadata(metadata("user"));
        user.getStatus().setPermalink("/authors/source");
        assertThat(userDescriptor.publiclyVisible(user)).isTrue();
        assertThat(userDescriptor.indexable(user)).isTrue();
        assertThat(userDescriptor.permalink(user)).isEqualTo("/authors/source");
        user.getStatus().setPermalink("");
        assertThat(userDescriptor.publiclyVisible(user)).isFalse();
    }

    private static Post post() {
        var post = new Post();
        var metadata = metadata("post");
        metadata.setLabels(Map.of(Post.PUBLISHED_LABEL, "true"));
        metadata.setAnnotations(Map.of(PREFERRED_EDITOR, "bytemd"));
        post.setMetadata(metadata);
        var spec = new Post.PostSpec();
        spec.setTitle("Source title");
        spec.setVisible(Post.VisibleEnum.PUBLIC);
        spec.setReleaseSnapshot("release");
        post.setSpec(spec);
        post.setStatus(new Post.PostStatus());
        return post;
    }

    private static SinglePage page() {
        var page = new SinglePage();
        var metadata = metadata("page");
        metadata.setLabels(Map.of(SinglePage.PUBLISHED_LABEL, "true"));
        page.setMetadata(metadata);
        var spec = new SinglePage.SinglePageSpec();
        spec.setTitle("Source title");
        spec.setVisible(Post.VisibleEnum.PUBLIC);
        spec.setReleaseSnapshot("release");
        page.setSpec(spec);
        page.setStatus(new SinglePage.SinglePageStatus());
        return page;
    }

    private static Metadata metadata(String name) {
        var metadata = new Metadata();
        metadata.setName(name);
        return metadata;
    }
}
