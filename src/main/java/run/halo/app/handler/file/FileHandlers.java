package run.halo.app.handler.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.exception.FileOperationException;
import run.halo.app.model.entity.Attachment;
import run.halo.app.model.enums.AttachmentType;
import run.halo.app.model.support.UploadResult;

import java.util.Collection;
import java.util.LinkedList;

/**
 * File handler manager.
 *
 * @author johnniang
 * @date 2019-03-27
 */
@Slf4j
@Component
public class FileHandlers {

    /**
     * File handler container.
     */
    private final Collection<FileHandler> fileHandlers = new LinkedList<>();

    public FileHandlers(ApplicationContext applicationContext) {
        // Add all file handler
        addFileHandlers(applicationContext.getBeansOfType(FileHandler.class).values());
    }

    /**
     * Uploads files.
     *
     * @param file           multipart file must not be null
     * @param attachmentType attachment type must not be null
     * @return upload result
     * @throws FileOperationException throws when fail to delete attachment or no available file handler to upload it
     */
    @NonNull
    public UploadResult upload(@NonNull MultipartFile file, @NonNull AttachmentType attachmentType) {
        Assert.notNull(file, "Multipart file must not be null");
        Assert.notNull(attachmentType, "Attachment type must not be null");

        for (FileHandler fileHandler : fileHandlers) {
            if (fileHandler.supportType(attachmentType)) {
                return fileHandler.upload(file);
            }
        }

        throw new FileOperationException("No available file handler to upload the file").setErrorData(attachmentType);
    }

    /**
     * Deletes attachment.
     *
     * @param attachment attachment detail must not be null
     * @throws FileOperationException throws when fail to delete attachment or no available file handler to delete it
     */
    public void delete(@NonNull Attachment attachment) {
        Assert.notNull(attachment, "Attachment must not be null");

        for (FileHandler fileHandler : fileHandlers) {
            if (fileHandler.supportType(attachment.getType())) {
                // Delete the file
                fileHandler.delete(attachment.getFileKey());
                return;
            }
        }

        throw new FileOperationException("No available file handler to delete the file").setErrorData(attachment);
    }

    /**
     * Adds file handlers.
     *
     * @param fileHandlers file handler collection
     * @return current file handlers
     */
    @NonNull
    public FileHandlers addFileHandlers(@Nullable Collection<FileHandler> fileHandlers) {
        if (!CollectionUtils.isEmpty(fileHandlers)) {
            this.fileHandlers.addAll(fileHandlers);
        }
        return this;
    }
}
