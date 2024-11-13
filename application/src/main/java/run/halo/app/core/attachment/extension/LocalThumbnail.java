package run.halo.app.core.attachment.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.lang.NonNull;
import run.halo.app.core.attachment.AttachmentRootGetter;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "storage.halo.run", version = "v1alpha1", kind = "LocalThumbnail",
    plural = "localthumbnails", singular = "localthumbnail")
public class LocalThumbnail extends AbstractExtension {
    public static final String UNIQUE_IMAGE_AND_SIZE_INDEX = "uniqueImageAndSize";
    public static final String REQUEST_TO_GENERATE_ANNO = "storage.halo.run/request-to-generate";

    @Schema(requiredMode = REQUIRED)
    private Spec spec;

    @Getter(onMethod_ = @NonNull)
    @Schema(requiredMode = NOT_REQUIRED)
    private Status status = new Status();

    public void setStatus(Status status) {
        this.status = (status == null ? new Status() : status);
    }

    @Data
    @Accessors(chain = true)
    @Schema(name = "LocalThumbnailSpec")
    public static class Spec {
        /**
         * A hash signature for the image uri.
         *
         * @see #getImageUri()
         */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String imageSignature;

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String imageUri;

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

    @Data
    @Schema(name = "LocalThumbnailStatus")
    public static class Status {
        private Phase phase;
    }

    public enum Phase {
        PENDING, SUCCEEDED, FAILED
    }

    public static String uniqueImageAndSize(LocalThumbnail localThumbnail) {
        return uniqueImageAndSize(localThumbnail.getSpec().getImageSignature(),
            localThumbnail.getSpec().getSize());
    }

    public static String uniqueImageAndSize(String imageSignature, ThumbnailSize size) {
        return imageSignature + "-" + size.name();
    }
}
