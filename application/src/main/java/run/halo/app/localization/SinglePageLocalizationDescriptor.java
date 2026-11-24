package run.halo.app.localization;

import static run.halo.app.localization.LocalizableSchema.text;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.content.SinglePageService;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.Extension;
import run.halo.app.extension.GroupVersionKind;

/**
 * Declares public display fields of SinglePage resources.
 *
 * @since 2.28.0
 */
@Component
public class SinglePageLocalizationDescriptor implements ResourceLocalizationDescriptor {
    private final SinglePageService singlePageService;

    public SinglePageLocalizationDescriptor() {
        this(null);
    }

    @Autowired
    public SinglePageLocalizationDescriptor(SinglePageService singlePageService) {
        this.singlePageService = singlePageService;
    }

    @Override
    public GroupVersionKind supports() {
        return GroupVersionKind.fromExtension(SinglePage.class);
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
        if (singlePageService == null || !(resource instanceof SinglePage page)) {
            return Mono.just(documents());
        }
        return singlePageService
                .getReleaseContent(page.getMetadata().getName())
                .map(content -> ContentLocalizationDocuments.documents(resource, content))
                .defaultIfEmpty(List.of())
                .onErrorResume(error -> Mono.just(List.of()));
    }

    @Override
    public Mono<Map<String, String>> resolveFields(Extension resource, Map<String, String> extracted) {
        if (singlePageService == null || !(resource instanceof SinglePage page)) {
            return Mono.just(extracted);
        }
        return singlePageService
                .getReleaseContent(page.getMetadata().getName())
                .map(content -> ContentLocalizationDocuments.fields(resource, extracted, content))
                .defaultIfEmpty(ContentLocalizationDocuments.fields(resource, extracted, null))
                .onErrorResume(error -> Mono.just(ContentLocalizationDocuments.fields(resource, extracted, null)));
    }

    @Override
    public boolean isEligible(Extension resource) {
        if (!(resource instanceof SinglePage page) || page.getSpec() == null) {
            return false;
        }
        return resource.getMetadata() != null
                && resource.getMetadata().getDeletionTimestamp() == null
                && !Boolean.TRUE.equals(page.getSpec().getDeleted());
    }

    @Override
    public boolean publiclyVisible(Extension resource) {
        if (!(resource instanceof SinglePage page) || !isEligible(resource) || !page.isPublished()) {
            return false;
        }
        var visible = page.getSpec().getVisible();
        return visible == null || visible == run.halo.app.core.extension.content.Post.VisibleEnum.PUBLIC;
    }

    @Override
    public boolean indexable(Extension resource) {
        return resource instanceof SinglePage page
                && publiclyVisible(resource)
                && PostLocalizationDescriptor.indexable(page.getSpec().getHtmlMetas());
    }

    @Override
    public String permalink(Extension resource) {
        if (!(resource instanceof SinglePage page) || page.getStatus() == null) {
            return "";
        }
        return Objects.toString(page.getStatus().getPermalink(), "");
    }

    @Override
    public String revision(Extension resource) {
        if (!(resource instanceof SinglePage page) || page.getSpec() == null) {
            return "";
        }
        return Objects.toString(page.getSpec().getReleaseSnapshot(), "");
    }

    @Override
    public Instant updatedAt(Extension resource) {
        if (resource instanceof SinglePage page
                && page.getStatus() != null
                && page.getStatus().getLastModifyTime() != null) {
            return page.getStatus().getLastModifyTime();
        }
        return ResourceLocalizationDescriptor.super.updatedAt(resource);
    }

    @Override
    public String displayName() {
        return "core.page.title";
    }

    @Override
    public String titleField() {
        return "title";
    }
}
