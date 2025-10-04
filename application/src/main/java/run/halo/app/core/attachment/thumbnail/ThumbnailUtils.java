package run.halo.app.core.attachment.thumbnail;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import run.halo.app.core.attachment.ThumbnailSize;

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
        if (!StringUtils.hasText(fileSuffix)) {
            return false;
        }
        return SUPPORTED_IMAGE_SUFFIXES.contains(fileSuffix.toLowerCase());
    }

    public static boolean isSupportedImage(MimeType mimeType) {
        return SUPPORTED_IMAGE_MIME_TYPES.stream()
            .anyMatch(supported -> supported.isCompatibleWith(mimeType));
    }

    /**
     * Build a map of thumbnail size to its corresponding URI based on the given permalink.
     *
     * @param permalink permalink of the attachment in local storage
     * @return a map where the key is the thumbnail size and the value is the URI of the thumbnail
     */
    public static Map<ThumbnailSize, URI> buildSrcsetMap(URI permalink) {
        var fileSuffix = FilenameUtils.getExtension(permalink.getPath());
        if (!isSupportedImage(fileSuffix)) {
            return Map.of();
        }
        return Arrays.stream(ThumbnailSize.values())
            .collect(Collectors.toMap(t -> t, t ->
                UriComponentsBuilder.fromUri(permalink)
                    .queryParam("width", t.getWidth())
                    .build(true)
                    .toUri()
            ));
    }
}
