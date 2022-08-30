package run.halo.app.core.extension.reconciler.attachment;

import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Constant;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;

public class AttachmentReconciler implements Reconciler<Request> {

    private final ExtensionClient client;

    public AttachmentReconciler(ExtensionClient client) {
        this.client = client;
    }

    @Override
    public Result reconcile(Request request) {
        client.fetch(Attachment.class, request.name()).ifPresent(attachment -> {
            var annotations = attachment.getMetadata().getAnnotations();
            if (annotations != null) {
                var localRelativePath = annotations.get(Constant.LOCAL_REL_PATH_ANNO_KEY);
                if (localRelativePath != null) {
                    // TODO Add router function here.
                } else {
                    var externalLink = annotations.get(Constant.EXTERNAL_LINK_ANNO_KEY);
                    if (externalLink != null) {
                        // TODO Set the external link into status
                        attachment.getStatus().setPermalink(externalLink);
                    }
                }
            }
        });

        return null;
    }

}
