package run.halo.app.handler.prehandler;

import java.io.ByteArrayOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
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

    @Override
    public MultipartFile preProcess(MultipartFile file) {
        try {
            if (ImageUtils.isImageType(file) && isRemoveExifEnable()) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageUtils.removeExifMetadataOut(file.getBytes(), outputStream);
                file = new ImageMultipartFile(outputStream.toByteArray(),
                    file.getOriginalFilename(),
                    file.getName(), file.getContentType());
            }
        } catch (Exception e) {
            log.info("Cannot check or remove Exif from this file.");
        }
        return file;
    }

    private boolean isRemoveExifEnable() {
        return optionService.getByPropertyOrDefault(AttachmentProperties.REMOVE_IMAGE_EXIF_ENABLE,
            Boolean.class, false);
    }

}
