package run.halo.app.core.extension.reconciler;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import run.halo.app.content.permalinks.TagPermalinkPolicy;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;

@Component
@RequiredArgsConstructor
public class TagRouteReconciler implements Reconciler<Reconciler.Request> {
    private final ExtensionClient client;
    private final TagPermalinkPolicy tagPermalinkPolicy;

    @Override
    public Result reconcile(Request request) {
        client.fetch(Tag.class, request.name())
            .ifPresent(tag -> {
                if (tag.getMetadata().getDeletionTimestamp() != null) {
                    // TagReconciler already did it, so there is no need to remove permalink
                    return;
                }

                reconcilePermalinkRoute(request.name());
            });
        return new Result(false, null);
    }

    private void reconcilePermalinkRoute(String tagName) {
        client.fetch(Tag.class, tagName)
            .ifPresent(tag -> {
                final String oldPermalink = tag.getStatusOrDefault().getPermalink();

                tagPermalinkPolicy.onPermalinkDelete(tag);

                String permalink = tagPermalinkPolicy.permalink(tag);
                tag.getStatusOrDefault().setPermalink(permalink);
                tagPermalinkPolicy.onPermalinkAdd(tag);

                if (!StringUtils.equals(permalink, oldPermalink)) {
                    client.update(tag);
                }
            });
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Tag())
            .build();
    }
}
