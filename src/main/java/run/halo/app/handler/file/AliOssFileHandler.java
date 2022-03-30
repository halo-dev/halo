package run.halo.app.handler.file;

import static run.halo.app.model.support.HaloConst.URL_SEPARATOR;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.PutObjectResult;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.exception.FileOperationException;
import run.halo.app.model.enums.AttachmentType;
import run.halo.app.model.properties.AliOssProperties;
import run.halo.app.model.support.UploadResult;
import run.halo.app.repository.AttachmentRepository;
import run.halo.app.service.OptionService;
import run.halo.app.utils.ImageUtils;

/**
 * Ali oss file handler.
 *
 * @author MyFaith
 * @author ryanwang
 * @author guqing
 * @date 2019-04-04
 */
@Slf4j
@Component
public class AliOssFileHandler implements FileHandler {

    private final OptionService optionService;
    private final AttachmentRepository attachmentRepository;

    public AliOssFileHandler(OptionService optionService,
        AttachmentRepository attachmentRepository) {
        this.optionService = optionService;
        this.attachmentRepository = attachmentRepository;
    }

    @Override
    public @NonNull UploadResult upload(@NonNull MultipartFile file) {
        Assert.notNull(file, "Multipart file must not be null");

        // Get config
        String protocol =
            optionService.getByPropertyOfNonNull(AliOssProperties.OSS_PROTOCOL).toString();
        String domain =
            optionService.getByPropertyOrDefault(AliOssProperties.OSS_DOMAIN, String.class, "");
        String source =
            optionService.getByPropertyOrDefault(AliOssProperties.OSS_SOURCE, String.class, "");
        String endPoint =
            optionService.getByPropertyOfNonNull(AliOssProperties.OSS_ENDPOINT).toString();
        String accessKey =
            optionService.getByPropertyOfNonNull(AliOssProperties.OSS_ACCESS_KEY).toString();
        String accessSecret =
            optionService.getByPropertyOfNonNull(AliOssProperties.OSS_ACCESS_SECRET).toString();
        String bucketName =
            optionService.getByPropertyOfNonNull(AliOssProperties.OSS_BUCKET_NAME).toString();
        String styleRule =
            optionService.getByPropertyOrDefault(AliOssProperties.OSS_STYLE_RULE, String.class, "");
        String thumbnailStyleRule = optionService
            .getByPropertyOrDefault(AliOssProperties.OSS_THUMBNAIL_STYLE_RULE, String.class, "");

        // Init OSS client
        OSS ossClient = new OSSClientBuilder().build(endPoint, accessKey, accessSecret);

        StringBuilder basePath = new StringBuilder(protocol);

        if (StringUtils.isNotEmpty(domain)) {
            basePath.append(domain)
                .append(URL_SEPARATOR);
        } else {
            basePath.append(bucketName)
                .append(".")
                .append(endPoint)
                .append(URL_SEPARATOR);
        }

        try {
            FilePathDescriptor uploadFilePath = new FilePathDescriptor.Builder()
                .setBasePath(basePath.toString())
                .setSubPath(source)
                .setAutomaticRename(true)
                .setRenamePredicate(relativePath ->
                    attachmentRepository
                        .countByFileKeyAndType(relativePath, AttachmentType.ALIOSS) > 0)
                .setOriginalName(file.getOriginalFilename())
                .build();

            log.info(basePath.toString());

            // Upload
            final PutObjectResult putObjectResult = ossClient.putObject(bucketName,
                uploadFilePath.getRelativePath(),
                file.getInputStream());

            if (putObjectResult == null) {
                throw new FileOperationException("上传附件 " + file.getOriginalFilename() + " 到阿里云失败 ");
            }

            // Response result
            final UploadResult uploadResult = new UploadResult();
            uploadResult.setFilename(uploadFilePath.getName());
            String fullPath = uploadFilePath.getFullPath();
            uploadResult
                .setFilePath(StringUtils.isBlank(styleRule) ? fullPath : fullPath + styleRule);
            uploadResult.setKey(uploadFilePath.getRelativePath());
            uploadResult
                .setMediaType(MediaType.valueOf(Objects.requireNonNull(file.getContentType())));
            uploadResult.setSuffix(uploadFilePath.getExtension());
            uploadResult.setSize(file.getSize());

            handleImageMetadata(file, uploadResult, () -> {
                if (ImageUtils.EXTENSION_ICO.equals(uploadFilePath.getExtension())) {
                    return fullPath;
                } else {
                    return StringUtils.isBlank(thumbnailStyleRule) ? fullPath :
                        fullPath + thumbnailStyleRule;
                }
            });

            log.info("Uploaded file: [{}] successfully", file.getOriginalFilename());
            return uploadResult;
        } catch (Exception e) {
            throw new FileOperationException("上传附件 " + file.getOriginalFilename() + " 到阿里云失败 ", e)
                .setErrorData(file.getOriginalFilename());
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public void delete(@NonNull String key) {
        Assert.notNull(key, "File key must not be blank");

        // Get config
        String endPoint =
            optionService.getByPropertyOfNonNull(AliOssProperties.OSS_ENDPOINT).toString();
        String accessKey =
            optionService.getByPropertyOfNonNull(AliOssProperties.OSS_ACCESS_KEY).toString();
        String accessSecret =
            optionService.getByPropertyOfNonNull(AliOssProperties.OSS_ACCESS_SECRET).toString();
        String bucketName =
            optionService.getByPropertyOfNonNull(AliOssProperties.OSS_BUCKET_NAME).toString();

        // Init OSS client
        OSS ossClient = new OSSClientBuilder().build(endPoint, accessKey, accessSecret);

        try {
            ossClient.deleteObject(new DeleteObjectsRequest(bucketName).withKey(key));
        } catch (Exception e) {
            throw new FileOperationException("附件 " + key + " 从阿里云删除失败", e);
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public AttachmentType getAttachmentType() {
        return AttachmentType.ALIOSS;
    }

}
