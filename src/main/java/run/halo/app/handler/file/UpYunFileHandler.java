package run.halo.app.handler.file;

import com.UpYun;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.exception.FileOperationException;
import run.halo.app.model.enums.AttachmentType;
import run.halo.app.model.properties.UpYunProperties;
import run.halo.app.model.support.UploadResult;
import run.halo.app.service.OptionService;
import run.halo.app.utils.FilenameUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Up Yun file handler.
 *
 * @author johnniang
 * @author ryanwang
 * @date 3/27/19
 */
@Slf4j
@Component
public class UpYunFileHandler implements FileHandler {

    private final OptionService optionService;

    public UpYunFileHandler(OptionService optionService) {
        this.optionService = optionService;
    }

    @Override
    public UploadResult upload(MultipartFile file) {
        Assert.notNull(file, "Multipart file must not be null");

        String ossSource = optionService.getByPropertyOfNonNull(UpYunProperties.OSS_SOURCE).toString();
        String ossPassword = optionService.getByPropertyOfNonNull(UpYunProperties.OSS_PASSWORD).toString();
        String ossBucket = optionService.getByPropertyOfNonNull(UpYunProperties.OSS_BUCKET).toString();
        String ossDomain = optionService.getByPropertyOfNonNull(UpYunProperties.OSS_DOMAIN).toString();
        String ossOperator = optionService.getByPropertyOfNonNull(UpYunProperties.OSS_OPERATOR).toString();
        // style rule can be null
        String ossStyleRule = optionService.getByPropertyOrDefault(UpYunProperties.OSS_STYLE_RULE, String.class, "");
        String ossThumbnailStyleRule = optionService.getByPropertyOrDefault(UpYunProperties.OSS_THUMBNAIL_STYLE_RULE, String.class, "");

        // Create up yun
        UpYun upYun = new UpYun(ossBucket, ossOperator, ossPassword);
        upYun.setDebug(log.isDebugEnabled());
        upYun.setTimeout(60);
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
                throw new FileOperationException("上传附件 " + file.getOriginalFilename() + " 到又拍云失败" + upFilePath);
            }

            String filePath = StringUtils.removeEnd(ossDomain, "/") + upFilePath;

            // Build upload result
            UploadResult uploadResult = new UploadResult();
            uploadResult.setFilename(basename);
            uploadResult.setFilePath(StringUtils.isBlank(ossStyleRule) ? filePath : filePath + ossStyleRule);
            uploadResult.setKey(upFilePath);
            uploadResult.setMediaType(MediaType.valueOf(Objects.requireNonNull(file.getContentType())));
            uploadResult.setSuffix(extension);
            uploadResult.setSize(file.getSize());

            // Handle thumbnail
            if (FileHandler.isImageType(uploadResult.getMediaType())) {
                BufferedImage image = ImageIO.read(file.getInputStream());
                uploadResult.setWidth(image.getWidth());
                uploadResult.setHeight(image.getHeight());
                uploadResult.setThumbPath(StringUtils.isBlank(ossThumbnailStyleRule) ? filePath : filePath + ossThumbnailStyleRule);
            }

            return uploadResult;
        } catch (Exception e) {
            throw new FileOperationException("上传附件 " + file.getOriginalFilename() + " 到又拍云失败", e);
        }
    }

    @Override
    public void delete(String key) {
        Assert.notNull(key, "File key must not be blank");

        // Get config
        String ossPassword = optionService.getByPropertyOfNonNull(UpYunProperties.OSS_PASSWORD).toString();
        String ossBucket = optionService.getByPropertyOfNonNull(UpYunProperties.OSS_BUCKET).toString();
        String ossOperator = optionService.getByPropertyOfNonNull(UpYunProperties.OSS_OPERATOR).toString();

        // Create up yun
        UpYun upYun = new UpYun(ossBucket, ossOperator, ossPassword);
        // Set api domain with ED_AUTO
        upYun.setApiDomain(UpYun.ED_AUTO);

        try {
            // Delete the file
            boolean deleteResult = upYun.deleteFile(key);
            if (!deleteResult) {
                log.warn("Failed to delete file " + key + " from UpYun");
            }
        } catch (Exception e) {
            throw new FileOperationException("附件 " + key + " 从又拍云删除失败", e);
        }
    }

    @Override
    public boolean supportType(AttachmentType type) {
        return AttachmentType.UPYUN.equals(type);
    }
}
