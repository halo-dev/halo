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
import run.halo.app.exception.RepeatTypeException;
import run.halo.app.model.entity.Attachment;
import run.halo.app.model.enums.AttachmentType;
import run.halo.app.model.support.UploadResult;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

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
    private final ConcurrentHashMap<AttachmentType, FileHandler> fileHandlers = new ConcurrentHashMap<>(16);

    public FileHandlers(ApplicationContext applicationContext) {
        // Add all file handler
        addFileHandlers(applicationContext.getBeansOfType(FileHandler.class).values());
        log.info("Registered {} file handler(s)", fileHandlers.size());
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
        return getSupportedType(attachmentType).upload(file);
    }


    /**
     * Deletes attachment.
     *
     * @param attachment attachment detail must not be null
     * @throws FileOperationException throws when fail to delete attachment or no available file handler to delete it
     */
    public void delete(@NonNull Attachment attachment) {
        Assert.notNull(attachment, "Attachment must not be null");
        getSupportedType(attachment.getType())
            .delete(attachment.getFileKey());
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
            for (FileHandler handler : fileHandlers) {
                if (this.fileHandlers.containsKey(handler.getAttachmentType())) {
                    throw new RepeatTypeException("Same attachment type implements must be unique");
                }
                this.fileHandlers.put(handler.getAttachmentType(), handler);
            }
        }
        return this;
    }

    private FileHandler getSupportedType(AttachmentType type) {
        FileHandler handler = fileHandlers.getOrDefault(type, fileHandlers.get(AttachmentType.LOCAL));
        if (handler == null) {
            throw new FileOperationException("No available file handlers to operate the file").setErrorData(type);
        }
        return handler;
    }
}
