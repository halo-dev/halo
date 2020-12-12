package run.halo.app.handler.file;


import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.bos.BosClient;
import com.baidubce.services.bos.BosClientConfiguration;
import com.baidubce.services.bos.model.PutObjectResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.exception.FileOperationException;
import run.halo.app.model.enums.AttachmentType;
import run.halo.app.model.properties.BaiduBosProperties;
import run.halo.app.model.support.UploadResult;
import run.halo.app.service.OptionService;
import run.halo.app.utils.FilenameUtils;
import run.halo.app.utils.ImageUtils;

import java.util.Objects;

/**
 * Baidu bos file handler.
 *
 * @author wangya
 * @author ryanwang
 * @date 2019-07-20
 */
@Slf4j
@Component
public class BaiduBosFileHandler implements FileHandler {

    private final OptionService optionService;

    public BaiduBosFileHandler(OptionService optionService) {
        this.optionService = optionService;
    }

    @Override
    public UploadResult upload(MultipartFile file) {
        Assert.notNull(file, "Multipart file must not be null");

        // Get config
        Object protocol = optionService.getByPropertyOfNonNull(BaiduBosProperties.BOS_PROTOCOL);
        String domain = optionService.getByPropertyOrDefault(BaiduBosProperties.BOS_DOMAIN, String.class, "");
        String endPoint = optionService.getByPropertyOfNonNull(BaiduBosProperties.BOS_ENDPOINT).toString();
        String accessKey = optionService.getByPropertyOfNonNull(BaiduBosProperties.BOS_ACCESS_KEY).toString();
        String secretKey = optionService.getByPropertyOfNonNull(BaiduBosProperties.BOS_SECRET_KEY).toString();
        String bucketName = optionService.getByPropertyOfNonNull(BaiduBosProperties.BOS_BUCKET_NAME).toString();
        String styleRule = optionService.getByPropertyOrDefault(BaiduBosProperties.BOS_STYLE_RULE, String.class, "");
        String thumbnailStyleRule = optionService.getByPropertyOrDefault(BaiduBosProperties.BOS_THUMBNAIL_STYLE_RULE, String.class, "");
        String source = StringUtils.join(protocol, bucketName, "." + endPoint);

        BosClientConfiguration config = new BosClientConfiguration();
        config.setCredentials(new DefaultBceCredentials(accessKey, secretKey));
        config.setEndpoint(endPoint);

        // Init OSS client
        BosClient client = new BosClient(config);

        domain = protocol + domain;

        try {
            String basename = FilenameUtils.getBasename(Objects.requireNonNull(file.getOriginalFilename()));
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            String timestamp = String.valueOf(System.currentTimeMillis());
            String upFilePath = StringUtils.join(basename, "_", timestamp, ".", extension);
            String filePath = StringUtils.join(StringUtils.appendIfMissing(StringUtils.isNotBlank(domain) ? domain : source, "/"), upFilePath);

            // Upload
            PutObjectResponse putObjectResponseFromInputStream = client.putObject(bucketName, upFilePath, file.getInputStream());
            if (putObjectResponseFromInputStream == null) {
                throw new FileOperationException("上传附件 " + file.getOriginalFilename() + " 到百度云失败 ");
            }

            // Response result
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
                    return filePath;
                } else {
                    return StringUtils.isBlank(thumbnailStyleRule) ? filePath : filePath + thumbnailStyleRule;
                }
            });

            return uploadResult;
        } catch (Exception e) {
            throw new FileOperationException("附件 " + file.getOriginalFilename() + " 上传失败(百度云)", e);
        } finally {
            client.shutdown();
        }
    }

    @Override
    public void delete(String key) {
        Assert.notNull(key, "File key must not be blank");

        // Get config
        String endPoint = optionService.getByPropertyOfNonNull(BaiduBosProperties.BOS_ENDPOINT).toString();
        String accessKey = optionService.getByPropertyOfNonNull(BaiduBosProperties.BOS_ACCESS_KEY).toString();
        String secretKey = optionService.getByPropertyOfNonNull(BaiduBosProperties.BOS_SECRET_KEY).toString();
        String bucketName = optionService.getByPropertyOfNonNull(BaiduBosProperties.BOS_BUCKET_NAME).toString();

        BosClientConfiguration config = new BosClientConfiguration();
        config.setCredentials(new DefaultBceCredentials(accessKey, secretKey));
        config.setEndpoint(endPoint);

        // Init OSS client
        BosClient client = new BosClient(config);

        try {
            client.deleteObject(bucketName, key);
        } catch (Exception e) {
            throw new FileOperationException("附件 " + key + " 从百度云删除失败", e);
        } finally {
            client.shutdown();
        }
    }

    @Override
    public AttachmentType getAttachmentType() {
        return AttachmentType.BAIDUBOS;
    }
}
