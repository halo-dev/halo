package run.halo.app.localization;

import static run.halo.app.localization.LocalizableSchema.text;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.content.PostService;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.Extension;
import run.halo.app.extension.GroupVersionKind;

/**
 * Declares public display fields of Post resources.
 *
 * @since 2.28.0
 */
@Component
public class PostLocalizationDescriptor implements ResourceLocalizationDescriptor {
    private final PostService postService;

    public PostLocalizationDescriptor() {
        this(null);
    }

    @Autowired
    public PostLocalizationDescriptor(PostService postService) {
        this.postService = postService;
    }

    @Override
    public GroupVersionKind supports() {
        return Post.GVK;
    }

    @Override
    public List<LocalizableField> fields() {
        return LocalizableField.list(
                LocalizableField.html("content.html", null).label("core.localization.field.body"),
                LocalizableField.raw("content.raw", null).label("core.localization.field.sourceBody"));
    }

    @Override
    public List<LocalizableFieldCollection> fieldCollections() {
        return List.of(LocalizableFieldCollection.builder("htmlMetas", "/spec/htmlMetas")
                .valueField("content")
                .group("core.localization.group.seo")
                .entries(
                        "name",
                        entries -> entries.add(
                                        "description",
                                        text().label("core.localization.field.seoDescription")
                                                .multiline(true))
                                .add("keywords", text().label("core.localization.field.seoKeywords"))
                                .add("twitter:title", text().label("core.localization.field.twitterTitle"))
                                .add(
                                        "twitter:description",
                                        text().label("core.localization.field.twitterDescription")
                                                .multiline(true)))
                .entries(
                        "property",
                        entries -> entries.add("og:title", text().label("core.localization.field.sharingTitle"))
                                .add(
                                        "og:description",
                                        text().label("core.localization.field.sharingDescription")
                                                .multiline(true)))
                .build());
    }

    @Override
    public List<LocalizableDocument> documents() {
        return List.of(new LocalizableDocument("content.html", "content.raw", "HTML", null));
    }

    @Override
    public List<String> linkPointers() {
        return List.of("/status/permalink");
    }

    @Override
    public Mono<List<LocalizableDocument>> resolveDocuments(Extension resource) {
        if (postService == null || !(resource instanceof Post post)) {
            return Mono.just(documents());
        }
        return postService
                .getReleaseContent(post)
                .map(content -> ContentLocalizationDocuments.documents(resource, content))
                .defaultIfEmpty(List.of())
                .onErrorResume(error -> Mono.just(List.of()));
    }

    @Override
    public Mono<Map<String, String>> resolveFields(Extension resource, Map<String, String> extracted) {
        if (postService == null || !(resource instanceof Post post)) {
            return Mono.just(extracted);
        }
        return postService
                .getReleaseContent(post)
                .map(content -> ContentLocalizationDocuments.fields(resource, extracted, content))
                .defaultIfEmpty(ContentLocalizationDocuments.fields(resource, extracted, null))
                .onErrorResume(error -> Mono.just(ContentLocalizationDocuments.fields(resource, extracted, null)));
    }

    @Override
    public boolean isEligible(Extension resource) {
        if (!(resource instanceof Post post) || post.getSpec() == null) {
            return false;
        }
        return resource.getMetadata() != null
                && resource.getMetadata().getDeletionTimestamp() == null
                && !post.isDeleted();
    }

    @Override
    public boolean publiclyVisible(Extension resource) {
        if (!(resource instanceof Post post) || !isEligible(resource)) {
            return false;
        }
        var spec = post.getSpec();
        return Post.isPublished(post.getMetadata()) && Post.isPublic(spec);
    }

    @Override
    public boolean indexable(Extension resource) {
        return resource instanceof Post post
                && publiclyVisible(resource)
                && indexable(post.getSpec().getHtmlMetas());
    }

    @Override
    public String permalink(Extension resource) {
        if (!(resource instanceof Post post) || post.getStatus() == null) {
            return "";
        }
        return Objects.toString(post.getStatus().getPermalink(), "");
    }

    @Override
    public String revision(Extension resource) {
        if (!(resource instanceof Post post) || post.getSpec() == null) {
            return "";
        }
        return Objects.toString(post.getSpec().getReleaseSnapshot(), "");
    }

    @Override
    public Instant updatedAt(Extension resource) {
        if (resource instanceof Post post
                && post.getStatus() != null
                && post.getStatus().getLastModifyTime() != null) {
            return post.getStatus().getLastModifyTime();
        }
        return ResourceLocalizationDescriptor.super.updatedAt(resource);
    }

    @Override
    public String displayName() {
        return "core.post.title";
    }

    @Override
    public String titleField() {
        return "title";
    }

    static boolean indexable(List<Map<String, String>> metas) {
        if (metas == null) {
            return true;
        }
        return metas.stream()
                .filter(meta -> "robots".equalsIgnoreCase(meta.get("name")))
                .map(meta -> meta.getOrDefault("content", "").toLowerCase(Locale.ROOT))
                .noneMatch(value -> Arrays.stream(value.split("[,\\s]+"))
                        .anyMatch(token -> token.equals("noindex") || token.equals("none")));
    }
}
