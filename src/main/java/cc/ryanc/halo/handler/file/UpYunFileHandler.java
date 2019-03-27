package cc.ryanc.halo.handler.file;

import cc.ryanc.halo.exception.FileOperationException;
import cc.ryanc.halo.model.enums.AttachmentType;
import cc.ryanc.halo.model.enums.UpYunProperties;
import cc.ryanc.halo.model.support.UploadResult;
import cc.ryanc.halo.service.OptionService;
import cc.ryanc.halo.utils.FilenameUtils;
import com.UpYun;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Up Yun file handler.
 *
 * @author johnniang
 * @date 3/27/19
 */
@Slf4j
public class UpYunFileHandler implements FileHandler {

    private final OptionService optionService;

    public UpYunFileHandler(OptionService optionService) {
        this.optionService = optionService;
    }

    @Override
    public UploadResult upload(MultipartFile file) {
        Assert.notNull(file, "Multipart file must not be null");

        String ossSource = optionService.getByPropertyOfNonNull(UpYunProperties.OSS_SOURCE);
        String ossPassword = optionService.getByPropertyOfNonNull(UpYunProperties.OSS_PASSWORD);
        String ossBucket = optionService.getByPropertyOfNonNull(UpYunProperties.OSS_BUCKET);
        String ossDomain = optionService.getByPropertyOfNonNull(UpYunProperties.OSS_DOMAIN);
        String ossOperator = optionService.getByPropertyOfNonNull(UpYunProperties.OSS_OPERATOR);
        // small url can be null
        String ossSmallUrl = optionService.getByPropertyOfNullable(UpYunProperties.OSS_SMALL_URL);

        // Create up yun
        UpYun upYun = new UpYun(ossBucket, ossOperator, ossPassword);
        upYun.setDebug(log.isDebugEnabled());
        upYun.setTimeout(60);
        // TODO Provide a property for choosing
        upYun.setApiDomain(UpYun.ED_AUTO);

        try {
            // Get file basename
            String basename = FilenameUtils.getBasename(file.getOriginalFilename());
            // Get file extension
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            // Get md5 value of the file
            String md5OfFile = DigestUtils.md5DigestAsHex(file.getInputStream());
            // Build file path
            String upFilePath = StringUtils.appendIfMissing(ossSource, "/") + md5OfFile + '.' + extension;
            // Set md5Content
            upYun.setContentMD5(md5OfFile);
            // Write file
            boolean uploadSuccess = upYun.writeFile(upFilePath, file.getInputStream(), true, null);
            if (!uploadSuccess) {
                throw new FileOperationException("Failed to upload file " + file.getOriginalFilename() + " to UpYun " + upFilePath);
            }

            String filePath = StringUtils.removeEnd(ossDomain, "/") + upFilePath;

            // Build upload result
            UploadResult uploadResult = new UploadResult();
            uploadResult.setFilename(basename);
            uploadResult.setFilePath(filePath);
            uploadResult.setKey(upFilePath);
            uploadResult.setMediaType(MediaType.valueOf(Objects.requireNonNull(file.getContentType())));
            uploadResult.setSuffix(extension);
            uploadResult.setSize(file.getSize());

            // Handle thumbnail
            if (FileHandler.isImageType(uploadResult.getMediaType())) {
                BufferedImage image = ImageIO.read(file.getInputStream());
                uploadResult.setWidth(image.getWidth());
                uploadResult.setHeight(image.getHeight());
                uploadResult.setThumbPath(StringUtils.isBlank(ossSmallUrl) ? filePath : filePath + ossSmallUrl);
            }

            return uploadResult;
        } catch (Exception e) {
            throw new FileOperationException("Failed to upload file " + file.getOriginalFilename() + " to UpYun", e);
        }
    }

    @Override
    public void delete(String key) {
        Assert.notNull(key, "File key must not be blank");

        // Get config
        String ossSource = optionService.getByPropertyOfNonNull(UpYunProperties.OSS_SOURCE);
        String ossPassword = optionService.getByPropertyOfNonNull(UpYunProperties.OSS_PASSWORD);
        String ossBucket = optionService.getByPropertyOfNonNull(UpYunProperties.OSS_BUCKET);
        String ossOperator = optionService.getByPropertyOfNonNull(UpYunProperties.OSS_OPERATOR);

        // Create up yun
        UpYun upYun = new UpYun(ossBucket, ossOperator, ossPassword);
        // Set api domain with ED_AUTO
        upYun.setApiDomain(UpYun.ED_AUTO);

        try {
            String filePath = ossSource + key;
            // Delete the file
            boolean deleteResult = upYun.deleteFile(filePath);
            if (!deleteResult) {
                log.warn("Failed to delete file " + filePath + " from UpYun");
            }
        } catch (Exception e) {
            throw new FileOperationException("Failed to delete file " + key + " from UpYun", e);
        }
    }

    @Override
    public boolean supportType(AttachmentType type) {
        return AttachmentType.UPYUN.equals(type);
    }
}
