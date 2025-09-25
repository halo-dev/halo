package run.halo.app.core.attachment;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;

public enum ThumbnailUtils {
    ;

    private static final Set<String> SUPPORTED_IMAGE_SUFFIXES = Set.of(
        "jpg", "jpeg", "png", "bmp", "wbmp"
    );

    private static final Set<MimeType> SUPPORTED_IMAGE_MIME_TYPES = Set.of(
            "image/jpg", "image/jpeg", "image/png", "image/bmp", "image/vnd.wap.wbmp"
        )
        .stream()
        .map(MediaType::parseMediaType)
        .collect(Collectors.toSet());

    /**
     * Check if the given file suffix is a supported image format for thumbnail generation.
     *
     * @param fileSuffix the file suffix to check (without the dot)
     * @return true if the file suffix is supported, false otherwise
     */
    public static boolean isSupportedImage(String fileSuffix) {
        return SUPPORTED_IMAGE_SUFFIXES.contains(fileSuffix.toLowerCase());
    }

    public static boolean isSupportedImage(MimeType mimeType) {
        return SUPPORTED_IMAGE_MIME_TYPES.stream()
            .anyMatch(supported -> supported.isCompatibleWith(mimeType));
    }
}
