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
import run.halo.app.model.properties.BaiDuYunProperties;
import run.halo.app.model.support.UploadResult;
import run.halo.app.service.OptionService;
import run.halo.app.utils.FilenameUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * BaiDuYun file handler.
 *
 * @author wangya
 * @date 2019-07-20
 */
@Slf4j
@Component
public class BaiDuYunFileHandler implements FileHandler {

    private final OptionService optionService;

    public BaiDuYunFileHandler(OptionService optionService) {
        this.optionService = optionService;
    }

    @Override
    public UploadResult upload(MultipartFile file) {
        Assert.notNull(file, "Multipart file must not be null");

        // Get config
        String bosDomain = optionService.getByPropertyOrDefault(BaiDuYunProperties.BOS_DOMAIN, String.class, "");
        String bosEndPoint = optionService.getByPropertyOfNonNull(BaiDuYunProperties.BOS_ENDPOINT).toString();
        String bosAccessKey = optionService.getByPropertyOfNonNull(BaiDuYunProperties.BOS_ACCESS_KEY).toString();
        String bosSecretKey = optionService.getByPropertyOfNonNull(BaiDuYunProperties.BOS_SECRET_KEY).toString();
        String bosBucketName = optionService.getByPropertyOfNonNull(BaiDuYunProperties.BOS_BUCKET_NAME).toString();
        String bosStyleRule = optionService.getByPropertyOrDefault(BaiDuYunProperties.BOS_STYLE_RULE, String.class, "");
        String bosThumbnailStyleRule = optionService.getByPropertyOrDefault(BaiDuYunProperties.BOS_THUMBNAIL_STYLE_RULE, String.class, "");
        String bosSource = StringUtils.join("https://", bosBucketName, "." + bosEndPoint);

        BosClientConfiguration config = new BosClientConfiguration();
        config.setCredentials(new DefaultBceCredentials(bosAccessKey, bosSecretKey));
        config.setEndpoint(bosEndPoint);

        // Init OSS client
        BosClient client = new BosClient(config);

        try {
            String basename = FilenameUtils.getBasename(file.getOriginalFilename());
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            String timestamp = String.valueOf(System.currentTimeMillis());
            String upFilePath = StringUtils.join(basename, "_", timestamp, ".", extension);
            String filePath = StringUtils.join(StringUtils.appendIfMissing(StringUtils.isNotBlank(bosDomain) ? bosDomain : bosSource, "/"), upFilePath);

            // Upload
            PutObjectResponse putObjectResponseFromInputStream = client.putObject(bosBucketName, upFilePath, file.getInputStream());
            if (putObjectResponseFromInputStream == null) {
                throw new FileOperationException("上传附件 " + file.getOriginalFilename() + " 到百度云失败 ");
            }

            // Response result
            UploadResult uploadResult = new UploadResult();
            uploadResult.setFilename(basename);
            uploadResult.setFilePath(StringUtils.isBlank(bosStyleRule) ? filePath : filePath + bosStyleRule);
            uploadResult.setKey(upFilePath);
            uploadResult.setMediaType(MediaType.valueOf(Objects.requireNonNull(file.getContentType())));
            uploadResult.setSuffix(extension);
            uploadResult.setSize(file.getSize());

            // Handle thumbnail
            if (FileHandler.isImageType(uploadResult.getMediaType())) {
                BufferedImage image = ImageIO.read(file.getInputStream());
                uploadResult.setWidth(image.getWidth());
                uploadResult.setHeight(image.getHeight());
                uploadResult.setThumbPath(StringUtils.isBlank(bosThumbnailStyleRule) ? filePath : filePath + bosThumbnailStyleRule);
            }

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
        String bosEndPoint = optionService.getByPropertyOfNonNull(BaiDuYunProperties.BOS_ENDPOINT).toString();
        String bosAccessKey = optionService.getByPropertyOfNonNull(BaiDuYunProperties.BOS_ACCESS_KEY).toString();
        String bosSecretKey = optionService.getByPropertyOfNonNull(BaiDuYunProperties.BOS_SECRET_KEY).toString();
        String bosBucketName = optionService.getByPropertyOfNonNull(BaiDuYunProperties.BOS_BUCKET_NAME).toString();

        BosClientConfiguration config = new BosClientConfiguration();
        config.setCredentials(new DefaultBceCredentials(bosAccessKey, bosSecretKey));
        config.setEndpoint(bosEndPoint);

        // Init OSS client
        BosClient client = new BosClient(config);

        try {
            client.deleteObject(bosBucketName, key);
        } catch (Exception e) {
            throw new FileOperationException("附件 " + key + " 从百度云删除失败", e);
        } finally {
            client.shutdown();
        }
    }

    @Override
    public boolean supportType(AttachmentType type) {
        return AttachmentType.BAIDUYUN.equals(type);
    }
}
