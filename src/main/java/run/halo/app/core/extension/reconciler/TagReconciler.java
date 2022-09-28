package run.halo.app.core.extension.reconciler;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import run.halo.app.content.permalinks.TagPermalinkPolicy;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.Tag;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Reconciler for {@link Tag}.
 *
 * @author guqing
 * @since 2.0.0
 */
public class TagReconciler implements Reconciler<Reconciler.Request> {
    private static final String FINALIZER_NAME = "tag-protection";
    private final ExtensionClient client;
    private final TagPermalinkPolicy tagPermalinkPolicy;

    public TagReconciler(ExtensionClient client, TagPermalinkPolicy tagPermalinkPolicy) {
        this.client = client;
        this.tagPermalinkPolicy = tagPermalinkPolicy;
    }

    @Override
    public Result reconcile(Request request) {
        return client.fetch(Tag.class, request.name())
            .map(tag -> {
                if (isDeleted(tag)) {
                    cleanUpResourcesAndRemoveFinalizer(request.name());
                    return new Result(false, null);
                }
                addFinalizerIfNecessary(tag);

                this.reconcileStatusPermalink(request.name());

                reconcileStatusPosts(request.name());
                return new Result(true, Duration.ofMinutes(1));
            })
            .orElseGet(() -> new Result(false, null));
    }

    private void cleanUpResources(Tag tag) {
        // remove permalink from permalink indexer
        tagPermalinkPolicy.onPermalinkDelete(tag);
    }

    private void addFinalizerIfNecessary(Tag oldTag) {
        Set<String> finalizers = oldTag.getMetadata().getFinalizers();
        if (finalizers != null && finalizers.contains(FINALIZER_NAME)) {
            return;
        }
        client.fetch(Tag.class, oldTag.getMetadata().getName())
            .ifPresent(tag -> {
                Set<String> newFinalizers = tag.getMetadata().getFinalizers();
                if (newFinalizers == null) {
                    newFinalizers = new HashSet<>();
                    tag.getMetadata().setFinalizers(newFinalizers);
                }
                newFinalizers.add(FINALIZER_NAME);
                client.update(tag);
            });
    }

    private void cleanUpResourcesAndRemoveFinalizer(String tagName) {
        client.fetch(Tag.class, tagName).ifPresent(tag -> {
            cleanUpResources(tag);
            if (tag.getMetadata().getFinalizers() != null) {
                tag.getMetadata().getFinalizers().remove(FINALIZER_NAME);
            }
            client.update(tag);
        });
    }

    private void reconcileStatusPermalink(String tagName) {
        client.fetch(Tag.class, tagName)
            .ifPresent(tag -> {
                Tag oldTag = JsonUtils.deepCopy(tag);
                tagPermalinkPolicy.onPermalinkDelete(oldTag);

                tag.getStatusOrDefault()
                    .setPermalink(tagPermalinkPolicy.permalink(tag));
                tagPermalinkPolicy.onPermalinkAdd(tag);

                if (!oldTag.equals(tag)) {
                    client.update(tag);
                }
            });
    }

    private void reconcileStatusPosts(String tagName) {
        client.fetch(Tag.class, tagName).ifPresent(tag -> {
            Tag oldTag = JsonUtils.deepCopy(tag);

            populatePosts(tag);

            if (!oldTag.equals(tag)) {
                client.update(tag);
            }
        });
    }

    private void populatePosts(Tag tag) {
        List<Post.CompactPost> compactPosts = client.list(Post.class, null, null)
            .stream()
            .filter(post -> includes(post.getSpec().getTags(), tag.getMetadata().getName()))
            .map(post -> Post.CompactPost.builder()
                .name(post.getMetadata().getName())
                .published(post.isPublished())
                .visible(post.getSpec().getVisible())
                .build())
            .toList();
        tag.getStatusOrDefault().setPostCount(compactPosts.size());

        long visiblePostCount = compactPosts.stream()
            .filter(post -> Objects.equals(true, post.getPublished())
                && Post.VisibleEnum.PUBLIC.equals(post.getVisible()))
            .count();
        tag.getStatusOrDefault().setVisiblePostCount((int) visiblePostCount);
    }

    private boolean includes(List<String> tags, String tagName) {
        if (tags == null) {
            return false;
        }
        return tags.contains(tagName);
    }

    private boolean isDeleted(Tag tag) {
        return tag.getMetadata().getDeletionTimestamp() != null;
    }
}
