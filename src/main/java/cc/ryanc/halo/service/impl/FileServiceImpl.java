package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.config.properties.HaloProperties;
import cc.ryanc.halo.exception.ServiceException;
import cc.ryanc.halo.model.support.UploadResult;
import cc.ryanc.halo.service.FileService;
import cc.ryanc.halo.service.OptionService;
import cc.ryanc.halo.utils.FilenameUtils;
import cc.ryanc.halo.utils.HaloUtils;
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
                           OptionService optionService) throws URISyntaxException {
        this.optionService = optionService;

        // Get work dir
        workDir = normalizeDirectory(haloProperties.getWorkDir());

        // Check directory
        checkWorkDir();

        log.info("Work directory: [{}]", workDir);
    }

    /**
     * Check work directory.
     *
     * @throws URISyntaxException throws when work directory is not a uri
     */
    private void checkWorkDir() throws URISyntaxException {
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
            uploadResult.setMediaType(MediaType.valueOf(file.getContentType()));
            uploadResult.setSize(file.getSize());

            // Check file type
            if (isImageType(file.getContentType())) {
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
