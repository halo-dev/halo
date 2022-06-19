package run.halo.app.utils;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import lombok.extern.slf4j.Slf4j;
import net.sf.image4j.codec.ico.ICODecoder;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.exception.ImageFormatException;

/**
 * @author ryanwang
 * @date 2019-12-10
 */
@Slf4j
public class ImageUtils {

    public static final String EXTENSION_ICO = "ico";

    public static BufferedImage getImageFromFile(InputStream is, String extension)
        throws IOException {
        log.debug("Current File type is : [{}]", extension);

        if (EXTENSION_ICO.equals(extension)) {
            try {
                return ICODecoder.read(is).get(0);
            } catch (IOException e) {
                throw new ImageFormatException("ico 文件已损坏", e);
            }
        } else {
            return ImageIO.read(is);
        }
    }

    @NonNull
    public static ImageReader getImageReaderFromFile(InputStream is, String formatName)
        throws IOException {
        try {
            Iterator<ImageReader> readerIterator = ImageIO.getImageReadersByFormatName(formatName);
            ImageReader reader = readerIterator.next();
            ImageInputStream stream = ImageIO.createImageInputStream(is);
            ImageIO.getImageReadersByFormatName(formatName);
            reader.setInput(stream, true);
            return reader;
        } catch (Exception e) {
            throw new IOException("Failed to read image reader.", e);
        }
    }

    public static boolean isImageType(@NonNull MultipartFile file) {
        MediaType IMAGE_TYPE = MediaType.valueOf("image/*");
        String mediaType = file.getContentType();
        return mediaType != null && IMAGE_TYPE.includes(MediaType.valueOf(mediaType));
    }

    /**
     * only remove exif in jpg/jpeg image for now
     */
    public static void removeExifMetadataOut(final byte[] src, final OutputStream dst)
        throws IOException, ImageReadException, ImageWriteException {
        try {
            OutputStream os = new BufferedOutputStream(dst);
            new ExifRewriter().removeExifMetadata(src, os);
            os.close();
        } catch (IOException | ImageReadException | ImageWriteException e) {
            throw new IOException("Failed to remove Exif metadata from image file.", e);
        }
    }
}
