package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.config.properties.HaloProperties;
import cc.ryanc.halo.exception.FileUploadException;
import cc.ryanc.halo.exception.ServiceException;
import cc.ryanc.halo.model.enums.QnYunProperties;
import cc.ryanc.halo.model.support.QiNiuPutSet;
import cc.ryanc.halo.model.support.UploadResult;
import cc.ryanc.halo.service.FileService;
import cc.ryanc.halo.service.OptionService;
import cc.ryanc.halo.utils.FilenameUtils;
import cc.ryanc.halo.utils.HaloUtils;
import cc.ryanc.halo.utils.JsonUtils;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.persistent.FileRecorder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Objects;

/**
 * File service implementation.
 *
 * @author johnniang
 * @date 3/26/19
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {

    private final OptionService optionService;

    private final String workDir;

    private final MediaType imageType = MediaType.valueOf("image/*");

    public FileServiceImpl(HaloProperties haloProperties,
                           OptionService optionService) {
        this.optionService = optionService;

        // Get work dir
        workDir = normalizeDirectory(haloProperties.getWorkDir());

        // Check directory
        checkWorkDir();

        log.info("Work directory: [{}]", workDir);
    }

    /**
     * Check work directory.
     */
    private void checkWorkDir() {
        // Get work path
        Path workPath = Paths.get(workDir);

        // Check file type
        Assert.isTrue(Files.isDirectory(workPath), workDir + " isn't a directory");

        // Check readable
        Assert.isTrue(Files.isReadable(workPath), workDir + " isn't readable");

        // Check writable
        Assert.isTrue(Files.isWritable(workPath), workDir + " isn't writable");
    }

    /**
     * Normalize directory full name, ensure the end path separator.
     *
     * @param dir directory full name must not be blank
     * @return normalized directory full name with end path separator
     */
    @NonNull
    private String normalizeDirectory(@NonNull String dir) {
        Assert.hasText(dir, "Directory full name must not be blank");

        return StringUtils.appendIfMissing(dir, File.separator);
    }

    @Override
    public UploadResult uploadToLocal(MultipartFile file) {
        Assert.notNull(file, "Multipart file must not be null");

        // Get current time
        Calendar current = Calendar.getInstance(optionService.getLocale());
        // Get month and day of month
        int year = current.get(Calendar.YEAR);
        int month = current.get(Calendar.MONTH) + 1;

        // Build directory
        String subDir = UPLOAD_SUB_DIR + File.separator + year + File.separator + month + File.separator;

        // Get basename
        String basename = FilenameUtils.getBasename(file.getOriginalFilename()) + '-' + HaloUtils.randomUUIDWithoutDash();

        // Get extension
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());

        log.debug("Base name: [{}], extension: [{}] of original filename: [{}]", basename, extension, file.getOriginalFilename());

        // Build sub file path
        String subFilePath = subDir + basename + '.' + extension;

        // Get upload path
        Path uploadPath = Paths.get(workDir + subFilePath);

        log.info("Uploading to directory: [{}]", uploadPath.getFileName());

        try {
            // TODO Synchronize here
            // Create directory
            Files.createDirectories(uploadPath.getParent());
            Files.createFile(uploadPath);

            // Upload this file
            file.transferTo(uploadPath);

            // Build upload result
            UploadResult uploadResult = new UploadResult();
            uploadResult.setFilename(basename);
            uploadResult.setFilePath(subFilePath);
            uploadResult.setSuffix(extension);
            uploadResult.setMediaType(MediaType.valueOf(Objects.requireNonNull(file.getContentType())));
            uploadResult.setSize(file.getSize());

            // Check file type
            if (isImageType(uploadResult.getMediaType())) {
                // Upload a thumbnail
                String thumbnailBasename = basename + '-' + "thumbnail";
                String thumbnailSubFilePath = subDir + thumbnailBasename + '.' + extension;
                Path thumbnailPath = Paths.get(workDir + thumbnailSubFilePath);

                // Create the thumbnail
                Files.createFile(thumbnailPath);

                // Generate thumbnail
                generateThumbnail(uploadPath, thumbnailPath);

                // Set thumb path
                uploadResult.setThumbPath(thumbnailSubFilePath);

                // Read as image
                BufferedImage image = ImageIO.read(Files.newInputStream(uploadPath));

                // Set width and height
                uploadResult.setWidth(image.getWidth());
                uploadResult.setHeight(image.getHeight());
            }

            return uploadResult;
        } catch (IOException e) {
            log.error("Failed to upload file to local: " + uploadPath.getFileName(), e);
            throw new ServiceException("Failed to upload file to local").setErrorData(uploadPath.getFileName());
        }
    }

    @Override
    public UploadResult uploadToQnYun(MultipartFile file) {
        Assert.notNull(file, "Multipart file must not be null");

        // Get all config
        Zone zone = optionService.getQnYunZone();
        String accessKey = optionService.getByPropertyOfNonNull(QnYunProperties.ACCESS_KEY);
        String secretKey = optionService.getByPropertyOfNonNull(QnYunProperties.SECRET_KEY);
        String bucket = optionService.getByPropertyOfNonNull(QnYunProperties.BUCKET);
        String domain = optionService.getByPropertyOfNonNull(QnYunProperties.DOMAIN);
        String smallUrl = optionService.getByPropertyOfNullable(QnYunProperties.SMALL_URL);

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
            result.setFilename(putSet.getHash());
            result.setFilePath(filePath);
            result.setSuffix(FilenameUtils.getExtension(file.getOriginalFilename()));
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

            throw new FileUploadException("Failed to upload file " + file.getOriginalFilename() + " to QnYun", e);
        }
    }

    @Override
    public UploadResult uploadToYpYun(MultipartFile file) {
        Assert.notNull(file, "Multipart file must not be null");
        return null;
    }

    /**
     * Generates thumbnail image.
     *
     * @param imagePath image path must not be null
     * @param thumbPath thumbnail path must not be null
     * @throws IOException throws if image provided is not valid
     */
    private void generateThumbnail(@NonNull Path imagePath, @NonNull Path thumbPath) throws IOException {
        Assert.notNull(imagePath, "Image path must not be null");
        Assert.notNull(thumbPath, "Thumb path must not be null");

        log.info("Generating thumbnail: [{}] for image: [{}]", thumbPath.getFileName(), imagePath.getFileName());

        // Convert to thumbnail and copy the thumbnail
        Thumbnails.of(imagePath.toFile()).size(THUMB_WIDTH, THUMB_HEIGHT).keepAspectRatio(true).toFile(thumbPath.toFile());
    }

    /**
     * Check whether media type provided is an image type.
     *
     * @param mediaType media type provided
     * @return true if it is an image type
     */
    private boolean isImageType(@Nullable String mediaType) {
        return mediaType != null && imageType.includes(MediaType.valueOf(mediaType));
    }


    /**
     * Check whether media type provided is an image type.
     *
     * @param mediaType media type provided
     * @return true if it is an image type
     */
    private boolean isImageType(@Nullable MediaType mediaType) {
        return mediaType != null && imageType.includes(mediaType);
    }
}
