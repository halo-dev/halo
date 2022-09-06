package run.halo.app.core.extension.reconciler;

import java.util.LinkedHashSet;
import java.util.Set;
import run.halo.app.content.permalinks.TagPermalinkPolicy;
import run.halo.app.core.extension.Tag;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.utils.JsonUtils;

/**
 * @author guqing
 * @since 2.0.0
 */
public class TagReconciler implements Reconciler<Reconciler.Request> {
    private static final String PERMALINK_FINALIZE = "permalink";
    private final ExtensionClient client;
    private final TagPermalinkPolicy tagPermalinkPolicy;

    public TagReconciler(ExtensionClient client, TagPermalinkPolicy tagPermalinkPolicy) {
        this.client = client;
        this.tagPermalinkPolicy = tagPermalinkPolicy;
    }

    @Override
    public Result reconcile(Request request) {
        client.fetch(Tag.class, request.name())
            .ifPresent(tag -> {
                Tag oldTag = JsonUtils.deepCopy(tag);

                finalizeFlag(tag);

                this.reconcilePermalink(tag);

                if (!tag.equals(oldTag)) {
                    client.update(tag);
                }
            });
        return new Result(false, null);
    }

    private void reconcilePermalink(Tag tag) {
        tagPermalinkPolicy.onPermalinkDelete(tag);

        tag.getStatusOrDefault()
            .setPermalink(tagPermalinkPolicy.permalink(tag));

        if (!isDeleted(tag)) {
            tagPermalinkPolicy.onPermalinkAdd(tag);
        }

        if (isDeleted(tag) && tag.getMetadata().getFinalizers() != null) {
            tag.getMetadata().getFinalizers().remove(PERMALINK_FINALIZE);
        }
    }

    private void finalizeFlag(Tag tag) {
        Set<String> finalizers = tag.getMetadata().getFinalizers();
        if (finalizers == null) {
            finalizers = new LinkedHashSet<>();
            tag.getMetadata().setFinalizers(finalizers);
        }
        finalizers.add(PERMALINK_FINALIZE);
    }

    private boolean isDeleted(Tag tag) {
        return tag.getMetadata().getDeletionTimestamp() != null;
    }
}
