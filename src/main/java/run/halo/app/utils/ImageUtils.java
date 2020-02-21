package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import net.sf.image4j.codec.ico.ICODecoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author ryanwang
 * @date 2019-12-10
 */
@Slf4j
public class ImageUtils {

    public static final String EXTENSION_ICO = "ico";

    public static BufferedImage getImageFromFile(InputStream is, String extension) throws IOException {
        log.debug("Current File type is : [{}]", extension);

        if (EXTENSION_ICO.equals(extension)) {
            return ICODecoder.read(is).get(0);
        } else {
            return ImageIO.read(is);
        }
    }
}
