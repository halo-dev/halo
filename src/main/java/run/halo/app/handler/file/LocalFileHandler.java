package run.halo.app.handler.file;

import static run.halo.app.model.support.HaloConst.FILE_SEPARATOR;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.exception.FileOperationException;
import run.halo.app.model.enums.AttachmentType;
import run.halo.app.model.support.UploadResult;
import run.halo.app.repository.AttachmentRepository;
import run.halo.app.utils.FilenameUtils;
import run.halo.app.utils.ImageUtils;

/**
 * Local file handler.
 *
 * @author johnniang
 * @author ryanwang
 * @author guqing
 * @date 2019-03-27
 */
@Slf4j
@Component
public class LocalFileHandler implements FileHandler {

    /**
     * Upload sub directory.
     */
    private static final String UPLOAD_SUB_DIR = "upload/";

    private static final String THUMBNAIL_SUFFIX = "-thumbnail";

    /**
     * Thumbnail width.
     */
    private static final int THUMB_WIDTH = 256;

    /**
     * Thumbnail height.
     */
    private static final int THUMB_HEIGHT = 256;

    private final AttachmentRepository attachmentRepository;

    private final String workDir;

    public LocalFileHandler(AttachmentRepository attachmentRepository,
        HaloProperties haloProperties) {
        this.attachmentRepository = attachmentRepository;

        // Get work dir
        workDir = FileHandler.normalizeDirectory(haloProperties.getWorkDir());

        // Check work directory
        checkWorkDir();
    }

    /**
     * Check work directory.
     */
    private void checkWorkDir() {
        // Get work path
        Path workPath = Paths.get(workDir);

        // Check file type
        if (!Files.isDirectory(workPath)
            || !Files.isReadable(workPath)
            || !Files.isWritable(workPath)) {
            log.warn("Please make sure that {} is a directory, readable and writable!", workDir);
        }
    }

    @NonNull
    @Override
    public UploadResult upload(@NonNull MultipartFile file) {
        Assert.notNull(file, "Multipart file must not be null");

        FilePathDescriptor uploadFilePath = new FilePathDescriptor.Builder()
            .setBasePath(workDir)
            .setSubPath(generatePath())
            .setSeparator(FILE_SEPARATOR)
            .setAutomaticRename(true)
            .setRenamePredicate(relativePath ->
                attachmentRepository
                    .countByFileKeyAndType(relativePath, AttachmentType.LOCAL) > 0)
            .setOriginalName(file.getOriginalFilename())
            .build();
        log.info("Uploading file: [{}] to directory: [{}]", file.getOriginalFilename(),
            uploadFilePath.getRelativePath());
        Path localFileFullPath = Paths.get(uploadFilePath.getFullPath());
        try {
            // TODO Synchronize here
            // Create directory
            Files.createDirectories(localFileFullPath.getParent());
            Files.createFile(localFileFullPath);

            // Upload this file
            file.transferTo(localFileFullPath);

            // Build upload result
            UploadResult uploadResult = new UploadResult();
            uploadResult.setFilename(uploadFilePath.getName());
            uploadResult.setFilePath(uploadFilePath.getRelativePath());
            uploadResult.setKey(uploadFilePath.getRelativePath());
            uploadResult.setSuffix(uploadFilePath.getExtension());
            uploadResult
                .setMediaType(MediaType.valueOf(Objects.requireNonNull(file.getContentType())));
            uploadResult.setSize(file.getSize());

            // TODO refactor this: if image is svg ext. extension
            handleImageMetadata(file, uploadResult, () -> {
                // Upload a thumbnail
                FilePathDescriptor thumbnailFilePath = new FilePathDescriptor.Builder()
                    .setBasePath(workDir)
                    .setSubPath(uploadFilePath.getSubPath())
                    .setSeparator(FILE_SEPARATOR)
                    .setOriginalName(uploadFilePath.getFullName())
                    .setNameSuffix(THUMBNAIL_SUFFIX)
                    .build();
                final Path thumbnailPath = Paths.get(thumbnailFilePath.getFullPath());
                try (InputStream is = file.getInputStream()) {
                    // Generate thumbnail
                    BufferedImage originalImage =
                        ImageUtils.getImageFromFile(is, uploadFilePath.getExtension());
                    boolean result = generateThumbnail(originalImage, thumbnailPath,
                        uploadFilePath.getExtension());
                    if (result) {
                        // Set thumb path
                        return thumbnailFilePath.getRelativePath();
                    }
                } catch (Throwable e) {
                    log.warn("Failed to open image file.", e);
                }
                return uploadFilePath.getRelativePath();
            });

            log.info("Uploaded file: [{}] to directory: [{}] successfully",
                file.getOriginalFilename(), uploadFilePath.getFullPath());
            return uploadResult;
        } catch (IOException e) {
            throw new FileOperationException("上传附件失败", e)
                .setErrorData(uploadFilePath.getFullPath());
        }
    }

    @Override
    public void delete(String key) {
        Assert.hasText(key, "File key must not be blank");
        // Get path
        Path path = Paths.get(workDir, key);

        // Delete the file key
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new FileOperationException("附件 " + key + " 删除失败", e);
        }

        // Delete thumb if necessary
        String basename = FilenameUtils.getBasename(key);
        String extension = FilenameUtils.getExtension(key);

        // Get thumbnail name
        String thumbnailName = basename + THUMBNAIL_SUFFIX + '.' + extension;

        // Get thumbnail path
        Path thumbnailPath = Paths.get(path.getParent().toString(), thumbnailName);

        // Delete thumbnail file
        try {
            boolean deleteResult = Files.deleteIfExists(thumbnailPath);
            if (!deleteResult) {
                log.warn("Thumbnail: [{}] may not exist", thumbnailPath.toString());
            }
        } catch (IOException e) {
            throw new FileOperationException("附件缩略图 " + thumbnailName + " 删除失败", e);
        }
    }

    @Override
    public AttachmentType getAttachmentType() {
        return AttachmentType.LOCAL;
    }

    private String generatePath() {
        // Get current time
        Calendar current = Calendar.getInstance();
        // Get month and day of month
        int year = current.get(Calendar.YEAR);
        int month = current.get(Calendar.MONTH) + 1;

        String monthString = month < 10 ? "0" + month : String.valueOf(month);

        // Build directory
        return UPLOAD_SUB_DIR + year + FILE_SEPARATOR + monthString + FILE_SEPARATOR;
    }

    private boolean generateThumbnail(BufferedImage originalImage, Path thumbPath,
        String extension) {
        Assert.notNull(originalImage, "Image must not be null");
        Assert.notNull(thumbPath, "Thumb path must not be null");

        boolean result = false;
        // Create the thumbnail
        try {
            Files.createFile(thumbPath);
            // Convert to thumbnail and copy the thumbnail
            log.debug("Trying to generate thumbnail: [{}]", thumbPath);
            Thumbnails.of(originalImage).size(THUMB_WIDTH, THUMB_HEIGHT).keepAspectRatio(true)
                .toFile(thumbPath.toFile());
            log.info("Generated thumbnail image, and wrote the thumbnail to [{}]", thumbPath);
            result = true;
        } catch (Throwable t) {
            // Ignore the error
            log.warn("Failed to generate thumbnail: " + thumbPath, t);
        } finally {
            // Disposes of this graphics context and releases any system resources that it is using.
            originalImage.getGraphics().dispose();
        }
        return result;
    }
}
