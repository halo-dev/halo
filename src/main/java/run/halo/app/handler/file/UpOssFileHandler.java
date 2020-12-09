package run.halo.app.handler.file;

import com.upyun.RestManager;
import com.upyun.UpException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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

        RestManager manager = new RestManager(bucket, operator, password);
        manager.setTimeout(60 * 10);
        manager.setApiDomain(RestManager.ED_AUTO);

        Map<String, String> params = new HashMap<>();

        try {
            // Get file basename
            String basename = FilenameUtils.getBasename(Objects.requireNonNull(file.getOriginalFilename()));
            // Get file extension
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            // Get md5 value of the file
            String md5OfFile = DigestUtils.md5DigestAsHex(file.getInputStream());
            // Build file path
            String upFilePath = StringUtils.appendIfMissing(source, "/") + md5OfFile + '.' + extension;
            // Set md5Content
            params.put(RestManager.PARAMS.CONTENT_MD5.getValue(), md5OfFile);
            // Write file
            Response result = manager.writeFile(upFilePath, file.getInputStream(), params);
            if (!result.isSuccessful()) {
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
            handleImageMetadata(file, uploadResult, () -> {
                if (ImageUtils.EXTENSION_ICO.equals(extension)) {
                    uploadResult.setThumbPath(filePath);
                    return filePath;
                } else {
                    return StringUtils.isBlank(thumbnailStyleRule) ? filePath : filePath + thumbnailStyleRule;
                }
            });
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

        RestManager manager = new RestManager(bucket, operator, password);
        manager.setTimeout(60 * 10);
        manager.setApiDomain(RestManager.ED_AUTO);

        try {
            Response result = manager.deleteFile(key, null);
            if (!result.isSuccessful()) {
                log.warn("附件 " + key + " 从又拍云删除失败");
            }
        } catch (IOException | UpException e) {
            e.printStackTrace();
            throw new FileOperationException("附件 " + key + " 从又拍云删除失败", e);
        }
    }

    @Override
    public AttachmentType getAttachmentType() {
        return AttachmentType.UPOSS;
    }
}
