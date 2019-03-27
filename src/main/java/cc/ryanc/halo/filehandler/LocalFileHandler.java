package cc.ryanc.halo.filehandler;

import cc.ryanc.halo.config.properties.HaloProperties;
import cc.ryanc.halo.exception.ServiceException;
import cc.ryanc.halo.model.enums.AttachmentType;
import cc.ryanc.halo.model.support.UploadResult;
import cc.ryanc.halo.service.OptionService;
import cc.ryanc.halo.utils.FilenameUtils;
import cc.ryanc.halo.utils.HaloUtils;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Objects;

/**
 * Local file handler.
 *
 * @author johnniang
 * @date 3/27/19
 */
@Slf4j
public class LocalFileHandler implements FileHandler {

    /**
     * Upload sub directory.
     */
    private final static String UPLOAD_SUB_DIR = "upload";

    /**
     * Thumbnail width.
     */
    private final static int THUMB_WIDTH = 256;

    /**
     * Thumbnail height.
     */
    private final static int THUMB_HEIGHT = 256;

    private final OptionService optionService;

    private final String workDir;

    public LocalFileHandler(OptionService optionService,
                            HaloProperties haloProperties) {
        this.optionService = optionService;

        // Get work dir
        workDir = FileHandler.normalizeDirectory(haloProperties.getWorkDir());

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

    @Override
    public UploadResult upload(MultipartFile file) {
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
            if (FileHandler.isImageType(uploadResult.getMediaType())) {
                // Upload a thumbnail
                String thumbnailBasename = basename + '-' + "thumbnail";
                String thumbnailSubFilePath = subDir + thumbnailBasename + '.' + extension;
                Path thumbnailPath = Paths.get(workDir + thumbnailSubFilePath);

                // Create the thumbnail
                Files.createFile(thumbnailPath);

                // Generate thumbnail
                generateThumbnail(uploadPath, thumbnailPath);

                // Read as image
                BufferedImage image = ImageIO.read(Files.newInputStream(uploadPath));

                // Set width and height
                uploadResult.setWidth(image.getWidth());
                uploadResult.setHeight(image.getHeight());

                // Set thumb path
                uploadResult.setThumbPath(thumbnailSubFilePath);
            }

            return uploadResult;
        } catch (IOException e) {
            log.error("Failed to upload file to local: " + uploadPath.getFileName(), e);
            throw new ServiceException("Failed to upload file to local").setErrorData(uploadPath.getFileName());
        }
    }

    @Override
    public boolean delete(String key) {
        return false;
    }

    @Override
    public boolean supportType(AttachmentType type) {
        return AttachmentType.LOCAL.equals(type);
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
}
