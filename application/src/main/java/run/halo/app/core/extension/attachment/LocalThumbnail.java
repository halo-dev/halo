package run.halo.app.core.extension.attachment;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import run.halo.app.core.attachment.AttachmentRootGetter;
import run.halo.app.core.attachment.ThumbnailSigner;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "storage.halo.run", version = "v1alpha1", kind = "LocalThumbnail",
    plural = "localthumbnails", singular = "localthumbnail")
public class LocalThumbnail extends AbstractExtension {

    @Schema(requiredMode = REQUIRED)
    private Spec spec;

    public static String signatureFor(String imageUri) {
        return ThumbnailSigner.generateSignature(imageUri);
    }

    @Data
    @Accessors(chain = true)
    @Schema(name = "LocalThumbnailSpec")
    public static class Spec {
        /**
         * A hash signature for the image uri.
         *
         * @see #getImageUrl()
         */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String imageSignature;

        /**
         * It must be an absolute uri to request the image.
         */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String imageUrl;

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String thumbnailUri;

        /**
         * A hash signature for the thumbnail uri.
         *
         * @see #getThumbnailUri()
         */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String thumbSignature;

        @Schema(requiredMode = REQUIRED)
        private ThumbnailSize size;

        /**
         * Consider the compatibility of the system and migration, use unix-style relative paths
         * here.
         *
         * @see AttachmentRootGetter
         */
        @Schema(requiredMode = REQUIRED)
        private String filePath;
    }
}
