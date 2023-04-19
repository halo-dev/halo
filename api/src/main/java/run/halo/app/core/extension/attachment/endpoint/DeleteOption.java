package run.halo.app.core.extension.attachment.endpoint;

import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.extension.ConfigMap;

public record DeleteOption(Attachment attachment, Policy policy, ConfigMap configMap)
    implements AttachmentHandler.DeleteContext {
}
