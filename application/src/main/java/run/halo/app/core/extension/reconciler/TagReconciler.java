package run.halo.app.core.extension.reconciler;

import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import run.halo.app.content.PostIndexInformer;
import run.halo.app.content.permalinks.TagPermalinkPolicy;
import run.halo.app.core.extension.content.Constant;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Reconciler for {@link Tag}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
public class TagReconciler implements Reconciler<Reconciler.Request> {
    private static final String FINALIZER_NAME = "tag-protection";
    private final ExtensionClient client;
    private final TagPermalinkPolicy tagPermalinkPolicy;
    private final PostIndexInformer postIndexInformer;

    @Override
    public Result reconcile(Request request) {
        return client.fetch(Tag.class, request.name())
            .map(tag -> {
                if (isDeleted(tag)) {
                    cleanUpResourcesAndRemoveFinalizer(request.name());
                    return Result.doNotRetry();
                }
                addFinalizerIfNecessary(tag);

                reconcileMetadata(request.name());

                this.reconcileStatusPermalink(request.name());

                reconcileStatusPosts(request.name());
                return new Result(true, Duration.ofMinutes(1));
            })
            .orElse(Result.doNotRetry());
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .syncAllOnStart(true)
            .extension(new Tag())
            .build();
    }

    void reconcileMetadata(String name) {
        client.fetch(Tag.class, name).ifPresent(tag -> {
            Map<String, String> annotations = MetadataUtil.nullSafeAnnotations(tag);
            String oldPermalinkPattern = annotations.get(Constant.PERMALINK_PATTERN_ANNO);

            String newPattern = tagPermalinkPolicy.pattern();
            annotations.put(Constant.PERMALINK_PATTERN_ANNO, newPattern);

            if (!StringUtils.equals(oldPermalinkPattern, newPattern)) {
                client.update(tag);
            }
        });
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
            if (tag.getMetadata().getFinalizers() != null) {
                tag.getMetadata().getFinalizers().remove(FINALIZER_NAME);
            }
            client.update(tag);
        });
    }

    private void reconcileStatusPermalink(String tagName) {
        client.fetch(Tag.class, tagName)
            .ifPresent(tag -> {
                String oldPermalink = tag.getStatusOrDefault().getPermalink();
                String permalink = tagPermalinkPolicy.permalink(tag);
                tag.getStatusOrDefault().setPermalink(permalink);

                if (!StringUtils.equals(permalink, oldPermalink)) {
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
        // populate post count
        Set<String> postNames = postIndexInformer.getByTagName(tag.getMetadata().getName());
        tag.getStatusOrDefault().setPostCount(postNames.size());

        // populate visible post count
        Map<String, String> labelToQuery = Map.of(Post.PUBLISHED_LABEL, BooleanUtils.TRUE,
            Post.VISIBLE_LABEL, Post.VisibleEnum.PUBLIC.name(),
            Post.DELETED_LABEL, BooleanUtils.FALSE);
        Set<String> hasAllLabelPosts = postIndexInformer.getByLabels(labelToQuery);

        // retain all posts that has all labels
        Set<String> postNamesWithTag = new HashSet<>(postNames);
        postNamesWithTag.retainAll(hasAllLabelPosts);
        tag.getStatusOrDefault().setVisiblePostCount(postNamesWithTag.size());
    }

    private boolean isDeleted(Tag tag) {
        return tag.getMetadata().getDeletionTimestamp() != null;
    }
}
