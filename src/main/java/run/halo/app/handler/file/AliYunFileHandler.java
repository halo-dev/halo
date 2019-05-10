package run.halo.app.handler.file;

import cn.hutool.core.lang.Assert;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.exception.FileOperationException;
import run.halo.app.model.enums.AttachmentType;
import run.halo.app.model.properties.AliYunProperties;
import run.halo.app.model.support.UploadResult;
import run.halo.app.service.OptionService;
import run.halo.app.utils.FilenameUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.Objects;

/**
 * AliYun file handler.
 * @author MyFaith
 * @date 2019-04-04 00:06:13
 */
@Slf4j
@Component
public class AliYunFileHandler implements FileHandler {

    private final OptionService optionService;

    public AliYunFileHandler(OptionService optionService) {
        this.optionService = optionService;
    }

    @Override
    public UploadResult upload(MultipartFile file) {
        Assert.notNull(file, "Multipart file must not be null");

        // Get config
        String ossEndPoint = optionService.getByPropertyOfNonNull(AliYunProperties.OSS_ENDPOINT).toString();
        String ossAccessKey = optionService.getByPropertyOfNonNull(AliYunProperties.OSS_ACCESS_KEY).toString();
        String ossAccessSecret = optionService.getByPropertyOfNonNull(AliYunProperties.OSS_ACCESS_SECRET).toString();
        String ossBucketName = optionService.getByPropertyOfNonNull(AliYunProperties.OSS_BUCKET_NAME).toString();
        String ossSource = StringUtils.join("https://", ossBucketName, "." + ossEndPoint);

        // Init OSS client
        OSS ossClient = new OSSClientBuilder().build(ossEndPoint, ossAccessKey, ossAccessSecret);

        try {
            String basename = FilenameUtils.getBasename(file.getOriginalFilename());
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            String timestamp = String.valueOf(System.currentTimeMillis());
            String upFilePath = StringUtils.join(basename, "_", timestamp, ".", extension);
            String filePath = StringUtils.join(StringUtils.appendIfMissing(ossSource, "/"), upFilePath);

            // Upload
            PutObjectResult putObjectResult = ossClient.putObject(ossBucketName, upFilePath, file.getInputStream());
            if (putObjectResult == null) {
                throw new FileOperationException("Failed to upload file " + file.getOriginalFilename() + " to AliYun " + upFilePath);
            }

            // Response result
            UploadResult uploadResult = new UploadResult();
            uploadResult.setFilename(basename);
            uploadResult.setFilePath(filePath);
            uploadResult.setKey(upFilePath);
            uploadResult.setMediaType(MediaType.valueOf(Objects.requireNonNull(file.getContentType())));
            uploadResult.setSuffix(extension);
            uploadResult.setSize(file.getSize());

            // Handle thumbnail
            if (FileHandler.isImageType(uploadResult.getMediaType())) {
                BufferedImage image = ImageIO.read(file.getInputStream());
                uploadResult.setWidth(image.getWidth());
                uploadResult.setHeight(image.getHeight());
                uploadResult.setThumbPath(filePath);
            }

            return uploadResult;
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            ossClient.shutdown();
        }

        // Build result
        UploadResult result = new UploadResult();

        log.info("File: [{}] uploaded successfully", file.getOriginalFilename());

        return result;
    }

    @Override
    public void delete(String key) {
        Assert.notNull(key, "File key must not be blank");

        // Get config
        String ossEndPoint = optionService.getByPropertyOfNonNull(AliYunProperties.OSS_ENDPOINT).toString();
        String ossAccessKey = optionService.getByPropertyOfNonNull(AliYunProperties.OSS_ACCESS_KEY).toString();
        String ossAccessSecret = optionService.getByPropertyOfNonNull(AliYunProperties.OSS_ACCESS_SECRET).toString();
        String ossBucketName = optionService.getByPropertyOfNonNull(AliYunProperties.OSS_BUCKET_NAME).toString();
        String ossSource = StringUtils.join("https://", ossBucketName, "." + ossEndPoint);

        // Init OSS client
        OSS ossClient = new OSSClientBuilder().build(ossEndPoint, ossAccessKey, ossAccessSecret);

        try {
            ossClient.deleteObject(new DeleteObjectsRequest(ossBucketName).withKey(key));
        } catch (Exception e) {
            throw new FileOperationException("Failed to delete file " + key + " from AliYun", e);
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public boolean supportType(AttachmentType type) {
        return AttachmentType.ALIYUN.equals(type);
    }
}
