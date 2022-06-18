package run.halo.app.handler.prehandler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 * @author eziosudo
 * @date 2022-06-16
 */
@Slf4j
@Component
public class AttachmentImagePreHandler implements FilePreHandler {

    private static final String REWRITE_IMAGE_EXTENSION = "image/jpeg";

    @Override
    public MultipartFile preProcess(MultipartFile multipartFile) {
       try {
            File dst =
                new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
            removeExifMetadata(multipartFile.getBytes(), dst);
            FileItem fileItem =
                new DiskFileItem("exif_removed", REWRITE_IMAGE_EXTENSION, false,
                    dst.getName(), (int) dst.length(), dst.getParentFile());
            new FileInputStream(dst).transferTo(fileItem.getOutputStream());
            multipartFile = new CommonsMultipartFile(fileItem);
            return multipartFile;
        } catch (Exception e) {
            log.info("Not JPG or JPEG format image. Cannot remove Exif.");
        }
        return multipartFile;
    }

    public void removeExifMetadata(final byte[] src, final File dst)
        throws IOException, ImageReadException, ImageWriteException {
        try (FileOutputStream fos = new FileOutputStream(dst);
             OutputStream os = new BufferedOutputStream(fos)) {
            new ExifRewriter().removeExifMetadata(src, os);
        }
    }

}
