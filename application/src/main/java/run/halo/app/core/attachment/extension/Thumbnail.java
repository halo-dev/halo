package run.halo.app.core.attachment.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "storage.halo.run", version = "v1alpha1", kind = "Thumbnail",
    plural = "thumbnails", singular = "thumbnail")
public class Thumbnail extends AbstractExtension {

    public static final String ID_INDEX = "thumbnail-id";

    @Schema(requiredMode = REQUIRED)
    private Spec spec;

    @Data
    @Accessors(chain = true)
    @Schema(name = "ThumbnailSpec")
    public static class Spec {
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String imageSignature;

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String imageUri;

        @Schema(requiredMode = REQUIRED)
        private ThumbnailSize size;

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String thumbnailUri;
    }

    public static String idIndexFunc(Thumbnail thumbnail) {
        return idIndexFunc(thumbnail.getSpec().getImageSignature(),
            thumbnail.getSpec().getSize().name());
    }

    public static String idIndexFunc(String imageHash, String size) {
        return imageHash + "-" + size;
    }
}
