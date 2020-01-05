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
import run.halo.app.model.properties.UpOssProperties;
import run.halo.app.model.support.UploadResult;
import run.halo.app.service.OptionService;
import run.halo.app.utils.FilenameUtils;
import run.halo.app.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Up oss file handler.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-27
 */
@Slf4j
@Component
public class UpOssFileHandler implements FileHandler {

    private final OptionService optionService;

    public UpOssFileHandler(OptionService optionService) {
        this.optionService = optionService;
    }

    @Override
    public UploadResult upload(MultipartFile file) {
        Assert.notNull(file, "Multipart file must not be null");

        String source = optionService.getByPropertyOfNonNull(UpOssProperties.OSS_SOURCE).toString();
        String password = optionService.getByPropertyOfNonNull(UpOssProperties.OSS_PASSWORD).toString();
        String bucket = optionService.getByPropertyOfNonNull(UpOssProperties.OSS_BUCKET).toString();
        String protocol = optionService.getByPropertyOfNonNull(UpOssProperties.OSS_PROTOCOL).toString();
        String domain = optionService.getByPropertyOfNonNull(UpOssProperties.OSS_DOMAIN).toString();
        String operator = optionService.getByPropertyOfNonNull(UpOssProperties.OSS_OPERATOR).toString();
        // style rule can be null
        String styleRule = optionService.getByPropertyOrDefault(UpOssProperties.OSS_STYLE_RULE, String.class, "");
        String thumbnailStyleRule = optionService.getByPropertyOrDefault(UpOssProperties.OSS_THUMBNAIL_STYLE_RULE, String.class, "");

        // Create up yun
        UpYun upYun = new UpYun(bucket, operator, password);
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
            String upFilePath = StringUtils.appendIfMissing(source, "/") + md5OfFile + '.' + extension;
            // Set md5Content
            upYun.setContentMD5(md5OfFile);
            // Write file
            boolean uploadSuccess = upYun.writeFile(upFilePath, file.getInputStream(), true, null);
            if (!uploadSuccess) {
                throw new FileOperationException("上传附件 " + file.getOriginalFilename() + " 到又拍云失败" + upFilePath);
            }

            String filePath = protocol + StringUtils.removeEnd(domain, "/") + upFilePath;

            // Build upload result
            UploadResult uploadResult = new UploadResult();
            uploadResult.setFilename(basename);
            uploadResult.setFilePath(StringUtils.isBlank(styleRule) ? filePath : filePath + styleRule);
            uploadResult.setKey(upFilePath);
            uploadResult.setMediaType(MediaType.valueOf(Objects.requireNonNull(file.getContentType())));
            uploadResult.setSuffix(extension);
            uploadResult.setSize(file.getSize());

            // Handle thumbnail
            if (FileHandler.isImageType(uploadResult.getMediaType())) {
                BufferedImage image = ImageUtils.getImageFromFile(file.getInputStream(), extension);
                uploadResult.setWidth(image.getWidth());
                uploadResult.setHeight(image.getHeight());
                if (ImageUtils.EXTENSION_ICO.equals(extension)) {
                    uploadResult.setThumbPath(filePath);
                } else {
                    uploadResult.setThumbPath(StringUtils.isBlank(thumbnailStyleRule) ? filePath : filePath + thumbnailStyleRule);
                }
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
        String password = optionService.getByPropertyOfNonNull(UpOssProperties.OSS_PASSWORD).toString();
        String bucket = optionService.getByPropertyOfNonNull(UpOssProperties.OSS_BUCKET).toString();
        String operator = optionService.getByPropertyOfNonNull(UpOssProperties.OSS_OPERATOR).toString();

        // Create up yun
        UpYun upYun = new UpYun(bucket, operator, password);
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
        return AttachmentType.UPOSS.equals(type);
    }
}
