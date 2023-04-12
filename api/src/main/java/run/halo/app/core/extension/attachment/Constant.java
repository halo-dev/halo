package run.halo.app.core.extension.attachment;

import run.halo.app.core.extension.attachment.endpoint.AttachmentHandler;

public enum Constant {
    ;

    public static final String GROUP = "storage.halo.run";
    public static final String VERSION = "v1alpha1";
    /**
     * The relative path starting from attachments folder is for deletion.
     */
    public static final String LOCAL_REL_PATH_ANNO_KEY = GROUP + "/local-relative-path";
    /**
     * The encoded URI is for building external url.
     */
    public static final String URI_ANNO_KEY = GROUP + "/uri";

    /**
     * Do not use this key to set external link. You could implement
     * {@link AttachmentHandler#getPermalink} by your self.
     * <p>
     *
     * @deprecated Use your own group instead.
     */
    public static final String EXTERNAL_LINK_ANNO_KEY = GROUP + "/external-link";

    public static final String FINALIZER_NAME = "attachment-manager";

}
