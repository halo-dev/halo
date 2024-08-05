package run.halo.app.infra.utils;

import java.io.IOException;
import java.io.InputStream;
import lombok.experimental.UtilityClass;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

@UtilityClass
public class FileTypeDetectUtils {

    private static final Tika tika = new Tika();

    /**
     * Detect mime type.
     *
     * @param inputStream input stream will be closed after detection.
     */
    public static String detectMimeType(InputStream inputStream) throws IOException {
        try {
            return tika.detect(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public static String detectFileExtension(String mimeType) throws MimeTypeException {
        MimeTypes mimeTypes = MimeTypes.getDefaultMimeTypes();
        return mimeTypes.forName(mimeType).getExtension();
    }
}
