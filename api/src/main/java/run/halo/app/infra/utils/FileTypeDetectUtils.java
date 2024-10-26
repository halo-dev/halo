package run.halo.app.infra.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.experimental.UtilityClass;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.util.Assert;

@UtilityClass
public class FileTypeDetectUtils {

    private static final Detector detector = new DefaultDetector();

    /**
     * <p>Detects the media type of the given document.</p>
     * <p>The type detection is based on the content of the given document stream and the name of
     * the document.</p>
     *
     * @param inputStream the document stream must not be null
     * @throws IOException if the stream can not be read
     */
    public static String detectMimeType(InputStream inputStream, String name) throws IOException {
        Assert.notNull(name, "The name of the document must not be null");
        var metadata = new Metadata();
        metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, name);
        return doDetectMimeType(inputStream, metadata);
    }

    /**
     * Detect mime type.
     *
     * @param inputStream input stream will be closed after detection, must not be null
     */
    public static String detectMimeType(InputStream inputStream) throws IOException {
        return doDetectMimeType(inputStream, new Metadata());
    }

    private static String doDetectMimeType(InputStream inputStream, Metadata metadata)
        throws IOException {
        Assert.notNull(inputStream, "The inputStream must not be null");
        try (var stream = (!inputStream.markSupported()
            ? new BufferedInputStream(inputStream) : inputStream)) {
            return detector.detect(stream, metadata).toString();
        }
    }

    public static String detectFileExtension(String mimeType) throws MimeTypeException {
        MimeTypes mimeTypes = MimeTypes.getDefaultMimeTypes();
        return mimeTypes.forName(mimeType).getExtension();
    }
}
