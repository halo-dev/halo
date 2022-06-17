package run.halo.app.handler.prehandler;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import run.halo.app.utils.FileUtils;
import run.halo.app.utils.FilenameUtils;

/**
 * @author eziosudo
 * @date 2022-06-16
 */
@Slf4j
@Component
public class AttachmentImagePreHandler implements FilePreHandler {

    private static final String REWRITE_IMAGE_EXTENSION = "jpeg";

    private static final int SIZE_THRESHOLD = 10240;

    @Override
    public MultipartFile preProcess(MultipartFile multipartFile) {
        // 如果不是图片文件就不处理直接返回，避免不必要的IO操作
        if (FilenameUtils.notImageFileExtension(multipartFile.getName())) {
            return multipartFile;
        }
        try {
            File file = FileUtils.convertMultipartFileToFile(multipartFile);
            
            //todo remove EXIF
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            Iterable<Directory> directories = metadata.getDirectories();
            while (directories.iterator().hasNext()){
                Directory next = directories.iterator().next();
                System.out.println(next.getName());
            }
            

            BufferedImage image = ImageIO.read(file);
            if (null != image) {
                ImageIO.write(image, REWRITE_IMAGE_EXTENSION, file);
                DiskFileItem fileItem = new DiskFileItem("fileData", "image/jpeg",
                    true, file.getName(), SIZE_THRESHOLD, file);
                fileItem.getOutputStream();
                return new CommonsMultipartFile(fileItem);
            }
        } catch (IOException e) {
            log.error("" + e);
            //todo 
            return multipartFile;
        } catch (ImageProcessingException e) {
            e.printStackTrace();
        }
        return multipartFile;
    }

}
