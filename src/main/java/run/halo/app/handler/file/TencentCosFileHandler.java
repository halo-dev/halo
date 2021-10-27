package run.halo.app.handler.file;

import static run.halo.app.model.support.HaloConst.URL_SEPARATOR;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.exception.FileOperationException;
import run.halo.app.model.enums.AttachmentType;
import run.halo.app.model.properties.TencentCosProperties;
import run.halo.app.model.support.UploadResult;
import run.halo.app.repository.AttachmentRepository;
import run.halo.app.service.OptionService;
import run.halo.app.utils.ImageUtils;

/**
 * Tencent cos file handler.
 *
 * @author wangya
 * @author ryanwang
 * @date 2019-07-25
 */
@Slf4j
@Component
public class TencentCosFileHandler implements FileHandler {

    private final OptionService optionService;
    private final AttachmentRepository attachmentRepository;

    public TencentCosFileHandler(OptionService optionService,
        AttachmentRepository attachmentRepository) {
        this.optionService = optionService;
        this.attachmentRepository = attachmentRepository;
    }

    @Override
    public UploadResult upload(MultipartFile file) {
        Assert.notNull(file, "Multipart file must not be null");

        // Get config
        String protocol =
            optionService.getByPropertyOfNonNull(TencentCosProperties.COS_PROTOCOL).toString();
        String domain =
            optionService.getByPropertyOrDefault(TencentCosProperties.COS_DOMAIN, String.class, "");
        String region =
            optionService.getByPropertyOfNonNull(TencentCosProperties.COS_REGION).toString();
        String secretId =
            optionService.getByPropertyOfNonNull(TencentCosProperties.COS_SECRET_ID).toString();
        String secretKey =
            optionService.getByPropertyOfNonNull(TencentCosProperties.COS_SECRET_KEY).toString();
        String bucketName =
            optionService.getByPropertyOfNonNull(TencentCosProperties.COS_BUCKET_NAME).toString();
        String source =
            optionService.getByPropertyOrDefault(TencentCosProperties.COS_SOURCE, String.class, "");
        String styleRule = optionService
            .getByPropertyOrDefault(TencentCosProperties.COS_STYLE_RULE, String.class, "");
        String thumbnailStyleRule = optionService
            .getByPropertyOrDefault(TencentCosProperties.COS_THUMBNAIL_STYLE_RULE, String.class,
                "");

        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        Region regionConfig = new Region(region);
        ClientConfig clientConfig = new ClientConfig(regionConfig);

        // Init OSS client
        COSClient cosClient = new COSClient(cred, clientConfig);

        StringBuilder basePath = new StringBuilder(protocol);

        if (StringUtils.isNotEmpty(domain)) {
            basePath.append(domain)
                .append(URL_SEPARATOR);
        } else {
            basePath.append(bucketName)
                .append(".cos.")
                .append(region)
                .append(".myqcloud.com")
                .append(URL_SEPARATOR);
        }

        try {
            FilePathDescriptor pathDescriptor = new FilePathDescriptor.Builder()
                .setBasePath(basePath.toString())
                .setSubPath(source)
                .setAutomaticRename(true)
                .setRenamePredicate(relativePath ->
                    attachmentRepository
                        .countByFileKeyAndType(relativePath, AttachmentType.TENCENTCOS) > 0)
                .setOriginalName(file.getOriginalFilename())
                .build();

            // Upload
            ObjectMetadata objectMetadata = new ObjectMetadata();
            //提前告知输入流的长度, 否则可能导致 oom
            objectMetadata.setContentLength(file.getSize());
            // 设置 Content type, 默认是 application/octet-stream
            objectMetadata.setContentType(file.getContentType());
            PutObjectResult putObjectResponseFromInputStream = cosClient
                .putObject(bucketName, pathDescriptor.getRelativePath(), file.getInputStream(),
                    objectMetadata);
            if (putObjectResponseFromInputStream == null) {
                throw new FileOperationException("上传附件 " + file.getOriginalFilename() + " 到腾讯云失败 ");
            }
            String fullPath = pathDescriptor.getFullPath();
            // Response result
            UploadResult uploadResult = new UploadResult();
            uploadResult.setFilename(pathDescriptor.getName());
            uploadResult
                .setFilePath(StringUtils.isBlank(styleRule) ? fullPath : fullPath + styleRule);
            uploadResult.setKey(pathDescriptor.getRelativePath());
            uploadResult
                .setMediaType(MediaType.valueOf(Objects.requireNonNull(file.getContentType())));
            uploadResult.setSuffix(pathDescriptor.getExtension());
            uploadResult.setSize(file.getSize());

            // Handle thumbnail
            handleImageMetadata(file, uploadResult, () -> {
                if (ImageUtils.EXTENSION_ICO.equals(pathDescriptor.getExtension())) {
                    uploadResult.setThumbPath(fullPath);
                    return fullPath;
                } else {
                    return StringUtils.isBlank(thumbnailStyleRule) ? fullPath :
                        fullPath + thumbnailStyleRule;
                }
            });
            return uploadResult;
        } catch (Exception e) {
            throw new FileOperationException("附件 " + file.getOriginalFilename() + " 上传失败(腾讯云)", e);
        } finally {
            cosClient.shutdown();
        }
    }

    @Override
    public void delete(String key) {
        Assert.notNull(key, "File key must not be blank");

        // Get config
        String region =
            optionService.getByPropertyOfNonNull(TencentCosProperties.COS_REGION).toString();
        String secretId =
            optionService.getByPropertyOfNonNull(TencentCosProperties.COS_SECRET_ID).toString();
        String secretKey =
            optionService.getByPropertyOfNonNull(TencentCosProperties.COS_SECRET_KEY).toString();
        String bucketName =
            optionService.getByPropertyOfNonNull(TencentCosProperties.COS_BUCKET_NAME).toString();

        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        Region regionConfig = new Region(region);
        ClientConfig clientConfig = new ClientConfig(regionConfig);

        // Init OSS client
        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            cosClient.deleteObject(bucketName, key);
        } catch (Exception e) {
            throw new FileOperationException("附件 " + key + " 从腾讯云删除失败", e);
        } finally {
            cosClient.shutdown();
        }
    }

    @Override
    public AttachmentType getAttachmentType() {
        return AttachmentType.TENCENTCOS;
    }
}
