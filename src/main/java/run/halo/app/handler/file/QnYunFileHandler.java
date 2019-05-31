package run.halo.app.handler.file;

import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.persistent.FileRecorder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.exception.FileOperationException;
import run.halo.app.model.enums.AttachmentType;
import run.halo.app.model.properties.QnYunProperties;
import run.halo.app.model.support.QiNiuPutSet;
import run.halo.app.model.support.UploadResult;
import run.halo.app.service.OptionService;
import run.halo.app.utils.FilenameUtils;
import run.halo.app.utils.JsonUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static run.halo.app.handler.file.FileHandler.isImageType;

/**
 * Qi niu yun file handler.
 *
 * @author johnniang
 * @date 3/27/19
 */
@Slf4j
@Component
public class QnYunFileHandler implements FileHandler {

    private final OptionService optionService;

    public QnYunFileHandler(OptionService optionService) {
        this.optionService = optionService;
    }

    @Override
    public UploadResult upload(MultipartFile file) {
        Assert.notNull(file, "Multipart file must not be null");

        // Get all config
        Zone zone = optionService.getQnYunZone();
        String accessKey = optionService.getByPropertyOfNonNull(QnYunProperties.OSS_ACCESS_KEY).toString();
        String secretKey = optionService.getByPropertyOfNonNull(QnYunProperties.OSS_SECRET_KEY).toString();
        String bucket = optionService.getByPropertyOfNonNull(QnYunProperties.OSS_BUCKET).toString();
        String domain = optionService.getByPropertyOfNonNull(QnYunProperties.OSS_DOMAIN).toString();
        String styleRule = optionService.getByPropertyOrDefault(QnYunProperties.OSS_STYLE_RULE, String.class, "");

        // Create configuration
        Configuration configuration = new Configuration(zone);

        // Create auth
        Auth auth = Auth.create(accessKey, secretKey);
        // Build put plicy
        StringMap putPolicy = new StringMap();
        putPolicy.put("returnBody", "{\"size\":$(fsize), " +
                "\"width\":$(imageInfo.width), " +
                "\"height\":$(imageInfo.height)," +
                " \"key\":\"$(key)\", " +
                "\"hash\":\"$(etag)\"}");
        // Get upload token
        String uploadToken = auth.uploadToken(bucket, null, 3600, putPolicy);

        // Create temp path
        Path tmpPath = Paths.get(System.getProperty("java.io.tmpdir"), bucket);


        try {
            String basename = FilenameUtils.getBasename(file.getOriginalFilename());
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());

            // Get file recorder for temp directory
            FileRecorder fileRecorder = new FileRecorder(tmpPath.toFile());
            // Get upload manager
            UploadManager uploadManager = new UploadManager(configuration, fileRecorder);
            // Put the file
            Response response = uploadManager.put(file.getInputStream(), null, uploadToken, null, null);

            log.debug("QnYun response: [{}]", response.toString());
            log.debug("QnYun response body: [{}]", response.bodyString());

            response.jsonToObject(QiNiuPutSet.class);

            // Convert response
            QiNiuPutSet putSet = JsonUtils.jsonToObject(response.bodyString(), QiNiuPutSet.class);

            // Get file full path
            String filePath = StringUtils.appendIfMissing(domain, "/") + putSet.getHash();

            // Build upload result
            UploadResult result = new UploadResult();
            result.setFilename(basename);
            result.setFilePath(filePath);
            result.setKey(putSet.getKey());
            result.setSuffix(extension);
            result.setWidth(putSet.getWidth());
            result.setHeight(putSet.getHeight());
            result.setMediaType(MediaType.valueOf(Objects.requireNonNull(file.getContentType())));
            result.setSize(file.getSize());

            if (isImageType(result.getMediaType())) {
                result.setThumbPath(StringUtils.isBlank(styleRule) ? filePath : filePath + styleRule);
            }

            return result;
        } catch (IOException e) {
            if (e instanceof QiniuException) {
                log.error("QnYun error response: [{}]", ((QiniuException) e).response);
            }

            throw new FileOperationException("上传附件 " + file.getOriginalFilename() + " 到七牛云失败", e);
        }
    }

    @Override
    public void delete(String key) {
        Assert.notNull(key, "File key must not be blank");

        // Get all config
        Zone zone = optionService.getQnYunZone();
        String accessKey = optionService.getByPropertyOfNonNull(QnYunProperties.OSS_ACCESS_KEY).toString();
        String secretKey = optionService.getByPropertyOfNonNull(QnYunProperties.OSS_SECRET_KEY).toString();
        String bucket = optionService.getByPropertyOfNonNull(QnYunProperties.OSS_BUCKET).toString();

        // Create configuration
        Configuration configuration = new Configuration(zone);

        // Create auth
        Auth auth = Auth.create(accessKey, secretKey);

        BucketManager bucketManager = new BucketManager(auth, configuration);

        try {
            bucketManager.delete(bucket, key);
        } catch (QiniuException e) {
            log.error("QnYun error response: [{}]", e.response);
            throw new FileOperationException("Failed to delete file with " + key + " key", e);
        }
    }

    @Override
    public boolean supportType(AttachmentType type) {
        return AttachmentType.QNYUN.equals(type);
    }
}
