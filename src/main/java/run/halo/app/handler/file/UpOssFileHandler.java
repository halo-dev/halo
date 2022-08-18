package run.halo.app.handler.file;

import com.upyun.RestManager;
import com.upyun.UpException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
import run.halo.app.repository.AttachmentRepository;
import run.halo.app.service.OptionService;
import run.halo.app.utils.ImageUtils;
import run.halo.app.utils.JsonUtils;

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
    private final AttachmentRepository attachmentRepository;

    public UpOssFileHandler(OptionService optionService,
        AttachmentRepository attachmentRepository) {
        this.optionService = optionService;
        this.attachmentRepository = attachmentRepository;
    }

    @Override
    public UploadResult upload(MultipartFile file) {
        Assert.notNull(file, "Multipart file must not be null");

        String source = optionService.getByPropertyOfNonNull(UpOssProperties.OSS_SOURCE).toString();
        String password =
            optionService.getByPropertyOfNonNull(UpOssProperties.OSS_PASSWORD).toString();
        String bucket = optionService.getByPropertyOfNonNull(UpOssProperties.OSS_BUCKET).toString();
        String protocol =
            optionService.getByPropertyOfNonNull(UpOssProperties.OSS_PROTOCOL).toString();
        String domain = optionService.getByPropertyOfNonNull(UpOssProperties.OSS_DOMAIN).toString();
        String operator =
            optionService.getByPropertyOfNonNull(UpOssProperties.OSS_OPERATOR).toString();
        // style rule can be null
        String styleRule =
            optionService.getByPropertyOrDefault(UpOssProperties.OSS_STYLE_RULE, String.class, "");
        String thumbnailStyleRule = optionService
            .getByPropertyOrDefault(UpOssProperties.OSS_THUMBNAIL_STYLE_RULE, String.class, "");

        RestManager manager = new RestManager(bucket, operator, password);
        manager.setTimeout(60 * 10);
        manager.setApiDomain(RestManager.ED_AUTO);

        Map<String, String> params = new HashMap<>();

        try {
            FilePathDescriptor pathDescriptor = new FilePathDescriptor.Builder()
                .setBasePath(protocol + domain)
                .setSubPath(source)
                .setAutomaticRename(true)
                .setRenamePredicate(relativePath ->
                    attachmentRepository
                        .countByFileKeyAndType(relativePath, AttachmentType.UPOSS) > 0)
                .setOriginalName(file.getOriginalFilename())
                .build();

            // Get md5 value of the file
            String md5OfFile = DigestUtils.md5DigestAsHex(file.getInputStream());
            // Set md5Content
            params.put(RestManager.PARAMS.CONTENT_MD5.getValue(), md5OfFile);

            String relativePath = pathDescriptor.getRelativePath();
            // Write file
            Response result = manager.writeFile(relativePath, file.getInputStream(), params);
            if (!result.isSuccessful()) {
                throw new FileOperationException(
                    "上传附件 " + file.getOriginalFilename() + " 到又拍云失败" + relativePath);
            }
            String fullPath = pathDescriptor.getFullPath();
            String extension = pathDescriptor.getExtension();
            // Build upload result
            UploadResult uploadResult = new UploadResult();
            uploadResult.setFilename(pathDescriptor.getName());
            uploadResult.setKey(relativePath);
            uploadResult.setSuffix(extension);
            uploadResult
                .setFilePath(StringUtils.isBlank(styleRule) ? fullPath : fullPath + styleRule);
            uploadResult
                .setMediaType(MediaType.valueOf(Objects.requireNonNull(file.getContentType())));
            uploadResult.setSize(file.getSize());

            // Handle thumbnail
            handleImageMetadata(file, uploadResult, () -> {
                if (ImageUtils.EXTENSION_ICO.equals(extension)) {
                    uploadResult.setThumbPath(fullPath);
                    return fullPath;
                } else {
                    return StringUtils.isBlank(thumbnailStyleRule) ? fullPath :
                        fullPath + thumbnailStyleRule;
                }
            });
            result.close();
            return uploadResult;
        } catch (Exception e) {
            throw new FileOperationException("上传附件 " + file.getOriginalFilename() + " 到又拍云失败", e);
        }
    }

    @Override
    public void delete(String key) {
        Assert.notNull(key, "File key must not be blank");

        // Get config
        String password =
            optionService.getByPropertyOfNonNull(UpOssProperties.OSS_PASSWORD).toString();
        String bucket = optionService.getByPropertyOfNonNull(UpOssProperties.OSS_BUCKET).toString();
        String operator =
            optionService.getByPropertyOfNonNull(UpOssProperties.OSS_OPERATOR).toString();

        RestManager manager = new RestManager(bucket, operator, password);
        manager.setTimeout(60 * 10);
        manager.setApiDomain(RestManager.ED_AUTO);

        try {
            Response result = manager.deleteFile(key, null);
            HashMap respondBody = JsonUtils.jsonToObject(result.body().string(), HashMap.class);
            if (!result.isSuccessful()
                && !(result.code() == 404 && respondBody.get("code").equals(40400001))) {
                log.warn("附件 " + key + " 从又拍云删除失败");
                throw new FileOperationException("附件 " + key + " 从又拍云删除失败");
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
