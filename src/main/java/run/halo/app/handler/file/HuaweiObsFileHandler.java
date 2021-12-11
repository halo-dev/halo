package run.halo.app.handler.file;

import static run.halo.app.model.support.HaloConst.URL_SEPARATOR;

import com.obs.services.ObsClient;
import com.obs.services.model.PutObjectResult;
import java.io.IOException;
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
import run.halo.app.model.properties.HuaweiObsProperties;
import run.halo.app.model.support.UploadResult;
import run.halo.app.repository.AttachmentRepository;
import run.halo.app.service.OptionService;
import run.halo.app.utils.ImageUtils;

/**
 * Huawei obs file handler.
 *
 * @author qilin
 * @author guqing
 * @date 2020-04-03
 */
@Slf4j
@Component
public class HuaweiObsFileHandler implements FileHandler {

    private final OptionService optionService;
    private final AttachmentRepository attachmentRepository;

    public HuaweiObsFileHandler(OptionService optionService,
        AttachmentRepository attachmentRepository) {
        this.optionService = optionService;
        this.attachmentRepository = attachmentRepository;
    }

    @Override
    public @NonNull UploadResult upload(@NonNull MultipartFile file) {
        Assert.notNull(file, "Multipart file must not be null");

        // Get config
        String protocol =
            optionService.getByPropertyOfNonNull(HuaweiObsProperties.OSS_PROTOCOL).toString();
        String domain =
            optionService.getByPropertyOrDefault(HuaweiObsProperties.OSS_DOMAIN, String.class, "");
        String source =
            optionService.getByPropertyOrDefault(HuaweiObsProperties.OSS_SOURCE, String.class, "");
        String endPoint =
            optionService.getByPropertyOfNonNull(HuaweiObsProperties.OSS_ENDPOINT).toString();
        String accessKey =
            optionService.getByPropertyOfNonNull(HuaweiObsProperties.OSS_ACCESS_KEY).toString();
        String accessSecret =
            optionService.getByPropertyOfNonNull(HuaweiObsProperties.OSS_ACCESS_SECRET).toString();
        String bucketName =
            optionService.getByPropertyOfNonNull(HuaweiObsProperties.OSS_BUCKET_NAME).toString();
        String styleRule = optionService
            .getByPropertyOrDefault(HuaweiObsProperties.OSS_STYLE_RULE, String.class, "");
        String thumbnailStyleRule = optionService
            .getByPropertyOrDefault(HuaweiObsProperties.OSS_THUMBNAIL_STYLE_RULE, String.class, "");

        // Init OSS client
        final ObsClient obsClient = new ObsClient(accessKey, accessSecret, endPoint);

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
            FilePathDescriptor pathDescriptor = new FilePathDescriptor.Builder()
                .setBasePath(basePath.toString())
                .setSubPath(source)
                .setAutomaticRename(true)
                .setRenamePredicate(relativePath ->
                    attachmentRepository
                        .countByFileKeyAndType(relativePath, AttachmentType.HUAWEIOBS) > 0)
                .setOriginalName(file.getOriginalFilename())
                .build();

            log.info(basePath.toString());

            // Upload
            PutObjectResult putObjectResult =
                obsClient.putObject(bucketName, pathDescriptor.getRelativePath(),
                    file.getInputStream());
            if (putObjectResult == null) {
                throw new FileOperationException("上传附件 " + file.getOriginalFilename() + " 到华为云失败 ");
            }

            // Response result
            UploadResult uploadResult = new UploadResult();
            uploadResult.setFilename(pathDescriptor.getName());
            String fullPath = pathDescriptor.getFullPath();
            uploadResult
                .setFilePath(StringUtils.isBlank(styleRule) ? fullPath : fullPath + styleRule);
            uploadResult.setKey(pathDescriptor.getRelativePath());
            uploadResult
                .setMediaType(MediaType.valueOf(Objects.requireNonNull(file.getContentType())));
            uploadResult.setSuffix(pathDescriptor.getExtension());
            uploadResult.setSize(file.getSize());

            handleImageMetadata(file, uploadResult, () -> {
                if (ImageUtils.EXTENSION_ICO.equals(pathDescriptor.getExtension())) {
                    return fullPath;
                } else {
                    return StringUtils.isBlank(thumbnailStyleRule) ? fullPath :
                        fullPath + thumbnailStyleRule;
                }
            });

            log.info("Uploaded file: [{}] successfully", file.getOriginalFilename());
            return uploadResult;
        } catch (Exception e) {
            throw new FileOperationException("上传附件 " + file.getOriginalFilename() + " 到华为云失败 ", e)
                .setErrorData(file.getOriginalFilename());
        } finally {
            try {
                obsClient.close();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    @Override
    public void delete(@NonNull String key) {
        Assert.notNull(key, "File key must not be blank");

        // Get config
        String endPoint =
            optionService.getByPropertyOfNonNull(HuaweiObsProperties.OSS_ENDPOINT).toString();
        String accessKey =
            optionService.getByPropertyOfNonNull(HuaweiObsProperties.OSS_ACCESS_KEY).toString();
        String accessSecret =
            optionService.getByPropertyOfNonNull(HuaweiObsProperties.OSS_ACCESS_SECRET).toString();
        String bucketName =
            optionService.getByPropertyOfNonNull(HuaweiObsProperties.OSS_BUCKET_NAME).toString();

        // Init OSS client
        final ObsClient obsClient = new ObsClient(accessKey, accessSecret, endPoint);

        try {
            obsClient.deleteObject(bucketName, key);
        } catch (Exception e) {
            throw new FileOperationException("附件 " + key + " 从华为云删除失败", e);
        } finally {
            try {
                obsClient.close();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    @Override
    public AttachmentType getAttachmentType() {
        return AttachmentType.HUAWEIOBS;
    }

}
