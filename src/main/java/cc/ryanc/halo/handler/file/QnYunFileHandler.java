package cc.ryanc.halo.handler.file;

import cc.ryanc.halo.exception.FileOperationException;
import cc.ryanc.halo.model.enums.AttachmentType;
import cc.ryanc.halo.model.enums.QnYunProperties;
import cc.ryanc.halo.model.support.QiNiuPutSet;
import cc.ryanc.halo.model.support.UploadResult;
import cc.ryanc.halo.service.OptionService;
import cc.ryanc.halo.utils.FilenameUtils;
import cc.ryanc.halo.utils.JsonUtils;
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
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static cc.ryanc.halo.handler.file.FileHandler.isImageType;

/**
 * Qi niu yun file handler.
 *
 * @author johnniang
 * @date 3/27/19
 */
@Slf4j
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
        String accessKey = optionService.getByPropertyOfNonNull(QnYunProperties.ACCESS_KEY);
        String secretKey = optionService.getByPropertyOfNonNull(QnYunProperties.SECRET_KEY);
        String bucket = optionService.getByPropertyOfNonNull(QnYunProperties.BUCKET);
        String domain = optionService.getByPropertyOfNonNull(QnYunProperties.DOMAIN);
        String smallUrl = optionService.getByPropertyOfNullable(QnYunProperties.SMALL_URL);

        // TODO Consider to cache the configuration
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
            // TODO May need to set key manually
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

            if (isImageType(result.getMediaType())) {
                result.setThumbPath(StringUtils.isBlank(smallUrl) ? filePath : filePath + smallUrl);
            }

            return result;
        } catch (IOException e) {
            if (e instanceof QiniuException) {
                log.error("QnYun error response: [{}]", ((QiniuException) e).response);
            }

            throw new FileOperationException("Failed to upload file " + file.getOriginalFilename() + " to QnYun", e);
        }
    }

    @Override
    public void delete(String key) {
        Assert.notNull(key, "File key must not be blank");

        // Get all config
        Zone zone = optionService.getQnYunZone();
        String accessKey = optionService.getByPropertyOfNonNull(QnYunProperties.ACCESS_KEY);
        String secretKey = optionService.getByPropertyOfNonNull(QnYunProperties.SECRET_KEY);
        String bucket = optionService.getByPropertyOfNonNull(QnYunProperties.BUCKET);

        // TODO Consider to cache the configuration
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
