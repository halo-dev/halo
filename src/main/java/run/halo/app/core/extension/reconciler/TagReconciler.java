package run.halo.app.core.extension.reconciler;

import run.halo.app.content.permalinks.TagPermalinkPolicy;
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

                this.reconcilePermalink(tag);

                if (!tag.equals(oldTag)) {
                    client.update(tag);
                }
            });
        return new Result(false, null);
    }

    private void reconcilePermalink(Tag tag) {
        tag.getStatusOrDefault()
            .setPermalink(tagPermalinkPolicy.permalink(tag));

        if (tag.getMetadata().getDeletionTimestamp() != null) {
            tagPermalinkPolicy.onPermalinkDelete(tag);
            return;
        }
        tagPermalinkPolicy.onPermalinkAdd(tag);
    }
}
