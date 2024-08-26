package run.halo.app.core.attachment;

import static run.halo.app.infra.FileCategoryMatcher.IMAGE;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import lombok.experimental.UtilityClass;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import run.halo.app.core.extension.attachment.Attachment;

@UtilityClass
public class AttachmentUtils {
    /**
     * Check whether the attachment is an image.
     *
     * @param attachment Attachment must not be null
     * @return true if the attachment is an image, false otherwise
     */
    public static boolean isImage(Attachment attachment) {
        Assert.notNull(attachment, "Attachment must not be null");
        var mediaType = attachment.getSpec().getMediaType();
        return mediaType != null && IMAGE.match(mediaType);
    }

    /**
     * Convert URI to URL.
     *
     * @param uri URI must not be null
     * @return URL
     * @throws IllegalArgumentException if the URL is malformed
     */
    public static URL toUrl(@NonNull URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Encode uri string to URI.
     * This method will decode the uri string first and then encode it.
     */
    public static URI encodeUri(String uriStr) {
        var decodedUriStr = UriUtils.decode(uriStr, StandardCharsets.UTF_8);
        return UriComponentsBuilder.fromUriString(decodedUriStr)
            .encode(StandardCharsets.UTF_8)
            .build()
            .toUri();
    }
}
