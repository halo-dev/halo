package run.halo.app.handler.prehandler;

import java.io.File;
import java.io.FileInputStream;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import run.halo.app.model.properties.AttachmentProperties;
import run.halo.app.service.OptionService;
import run.halo.app.utils.ImageUtils;

/**
 * @author eziosudo
 * @date 2022-06-16
 */
@Slf4j
@Component
public class ImageFilePreHandler implements FilePreHandler {

    @Autowired
    private OptionService optionService;

    private static final String REWRITE_IMAGE_EXTENSION = "image/jpeg";

    @Override
    public MultipartFile preProcess(MultipartFile multipartFile) {
        try {
            if (isRemoveExifEnable()) {
                File dst = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
                ImageUtils.removeExifMetadata(multipartFile.getBytes(), dst);
                FileItem fileItem =
                    new DiskFileItem("remove_exif", REWRITE_IMAGE_EXTENSION, false,
                        dst.getName(), (int) dst.length(), dst.getParentFile());
                new FileInputStream(dst).transferTo(fileItem.getOutputStream());
                multipartFile = new CommonsMultipartFile(fileItem);
            }
        } catch (Exception e) {
            log.info("Cannot check or remove Exif from this file.");
        }
        return multipartFile;
    }

    private boolean isRemoveExifEnable() {
        return optionService.getByPropertyOrDefault(AttachmentProperties.REMOVE_IMAGE_EXIF_ENABLE,
            Boolean.class, false);
    }


}
