package run.halo.app.search.post;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.ContentWrapper;
import run.halo.app.content.PostService;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.index.query.QueryFactory;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.infra.ReactiveExtensionPaginatedOperator;
import run.halo.app.search.HaloDocument;
import run.halo.app.search.HaloDocumentsProvider;

@Component
public class PostHaloDocumentsProvider implements HaloDocumentsProvider {

    public static final String POST_DOCUMENT_TYPE = "post.content.halo.run";

    private final ReactiveExtensionPaginatedOperator paginatedOperator;

    private final PostService postService;

    public PostHaloDocumentsProvider(ReactiveExtensionPaginatedOperator paginatedOperator,
        PostService postService) {
        this.paginatedOperator = paginatedOperator;
        this.postService = postService;
    }

    @Override
    public Flux<HaloDocument> fetchAll() {
        // make sure the posts are published, public visible and not deleted.
        var options = new ListOptions();
        var noteDeleted = QueryFactory.isNull("metadata.deletionTimestamp");
        options.setFieldSelector(FieldSelector.of(noteDeleted));
        // get content
        return paginatedOperator.list(Post.class, options)
            .flatMap(post -> postService.getReleaseContent(post)
                .switchIfEmpty(Mono.fromSupplier(() -> ContentWrapper.builder()
                    .content("")
                    .raw("")
                    .rawType("")
                    .build()))
                .map(contentWrapper -> convert(post, contentWrapper))
            );
    }

    @Override
    public String getType() {
        return POST_DOCUMENT_TYPE;
    }

    /**
     * Converts post to HaloDocument.
     *
     * @param post post detail
     * @param content post content
     * @return halo document
     */
    public static HaloDocument convert(Post post, ContentWrapper content) {
        var haloDoc = new HaloDocument();
        var spec = post.getSpec();
        haloDoc.setMetadataName(post.getMetadata().getName());
        haloDoc.setType(POST_DOCUMENT_TYPE);
        haloDoc.setId(POST_DOCUMENT_TYPE + '-' + post.getMetadata().getName());
        haloDoc.setTitle(spec.getTitle());
        haloDoc.setDescription(post.getStatus().getExcerpt());
        haloDoc.setPublished(Post.isPublished(post.getMetadata()));
        haloDoc.setRecycled(Post.isRecycled(post.getMetadata()));
        haloDoc.setExposed(Post.isPublic(spec));
        haloDoc.setContent(content.getContent());
        haloDoc.setTags(spec.getTags());
        haloDoc.setCategories(spec.getCategories());
        haloDoc.setOwnerName(spec.getOwner());
        haloDoc.setUpdateTimestamp(spec.getPublishTime());
        haloDoc.setCreationTimestamp(post.getMetadata().getCreationTimestamp());
        haloDoc.setPermalink(post.getStatus().getPermalink());
        return haloDoc;
    }
}
