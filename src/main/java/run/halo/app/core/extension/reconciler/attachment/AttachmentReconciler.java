package run.halo.app.core.extension.reconciler.attachment;

import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriUtils;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Attachment.AttachmentStatus;
import run.halo.app.core.extension.attachment.Constant;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;

@Slf4j
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
                String permalink = null;
                var localRelativePath = annotations.get(Constant.LOCAL_REL_PATH_ANNO_KEY);
                if (localRelativePath != null) {
                    // TODO Add router function here.
                    permalink = "http://localhost:8090/upload/" + localRelativePath;
                    permalink = UriUtils.encodePath(permalink, StandardCharsets.UTF_8);
                } else {
                    var externalLink = annotations.get(Constant.EXTERNAL_LINK_ANNO_KEY);
                    if (externalLink != null) {
                        // TODO Set the external link into status
                        permalink = externalLink;
                    }
                }
                if (permalink != null) {
                    log.debug("Set permalink {} for attachment {}", permalink, request.name());
                    var status = attachment.getStatus();
                    if (status == null) {
                        status = new AttachmentStatus();
                    }
                    status.setPermalink(permalink);

                    // update status
                    attachment.setStatus(status);
                    client.update(attachment);
                }
            }
        });

        return null;
    }

}
