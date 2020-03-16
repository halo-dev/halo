package run.halo.app.handler.file;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.exception.FileOperationException;
import run.halo.app.model.enums.AttachmentType;
import run.halo.app.model.support.UploadResult;

import static run.halo.app.model.support.HaloConst.FILE_SEPARATOR;

/**
 * File handler interface.
 *
 * @author johnniang
 * @date 2019-03-27
 */
public interface FileHandler {

    MediaType IMAGE_TYPE = MediaType.valueOf("image/*");

    /**
     * Check whether media type provided is an image type.
     *
     * @param mediaType media type provided
     * @return true if it is an image type
     */
    static boolean isImageType(@Nullable String mediaType) {
        return mediaType != null && IMAGE_TYPE.includes(MediaType.valueOf(mediaType));
    }

    /**
     * Check whether media type provided is an image type.
     *
     * @param mediaType media type provided
     * @return true if it is an image type
     */
    static boolean isImageType(@Nullable MediaType mediaType) {
        return mediaType != null && IMAGE_TYPE.includes(mediaType);
    }

    /**
     * Normalize directory full name, ensure the end path separator.
     *
     * @param dir directory full name must not be blank
     * @return normalized directory full name with end path separator
     */
    @NonNull
    static String normalizeDirectory(@NonNull String dir) {
        Assert.hasText(dir, "Directory full name must not be blank");

        return StringUtils.appendIfMissing(dir, FILE_SEPARATOR);
    }

    /**
     * Uploads file.
     *
     * @param file multipart file must not be null
     * @return upload result
     * @throws FileOperationException throws when fail to upload the file
     */
    @NonNull
    UploadResult upload(@NonNull MultipartFile file);

    /**
     * Deletes file.
     *
     * @param key file key must not be null
     * @throws FileOperationException throws when fail to delete the file
     */
    void delete(@NonNull String key);

    /**
     * Get attachment type is supported.
     *
     * @return attachment type
     */
    AttachmentType getAttachmentType();
}