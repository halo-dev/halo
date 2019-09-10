package run.halo.app.utils;

import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;

/**
 * ImageUtils.
 *
 * @author johnniang
 */
@Slf4j
public class ImageUtils {

    private ImageUtils() {
        ImageIO.setUseCache(true);
        ImageIO.setCacheDirectory(Files.createTempDir());
    }


    public static void writeImage(BufferedImage image, String filepath, String extension) throws IOException {
        File file = new File(filepath);

        ImageIO.write(image, extension, file);
    }

    public static BufferedImage readImage(File file) throws IOException {
        Assert.notNull(file, "file must not be null");

        return ImageIO.read(file);
    }

    public static BufferedImage readImage(InputStream inputStream) throws IOException {
        Assert.notNull(inputStream, "input stream must not be null");

        return ImageIO.read(inputStream);
    }

    public static BufferedImage readImage(byte[] buf) throws IOException {
        Assert.notNull(buf, "image byte array must not be null");


        return ImageIO.read(new ByteArrayInputStream(buf));
    }

    public static void compress(BufferedImage originalImage, float quantity, OutputStream os) throws IOException {
        Assert.notNull(originalImage, "original image must not be null");

        Iterator<ImageWriter> imageWriterIterator = ImageIO.getImageWritersByFormatName("jpg");

        ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(os);

        ImageWriter imageWriter = imageWriterIterator.next();
        imageWriter.setOutput(imageOutputStream);

        ImageWriteParam param = imageWriter.getDefaultWriteParam();

        if (param.canWriteCompressed()) {
            param.setCompressionMode((ImageWriteParam.MODE_EXPLICIT));
            param.setCompressionQuality(quantity);
        }

        imageWriter.write(null, new IIOImage(originalImage, null, null), param);

        imageWriter.dispose();
    }
}
