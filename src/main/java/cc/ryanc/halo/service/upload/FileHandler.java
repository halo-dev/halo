package cc.ryanc.halo.service.upload;

import cc.ryanc.halo.exception.FileUploadException;
import cc.ryanc.halo.model.support.UploadResult;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

/**
 * File handler interface.
 *
 * @author johnniang
 * @date 3/27/19
 */
public interface FileHandler {

    /**
     * Uploads file.
     *
     * @param file multipart file must not be null
     * @return upload result
     * @throws FileUploadException throws when fail to upload the file
     */
    @NonNull
    UploadResult upload(@NonNull MultipartFile file);

    /**
     * Deletes file.
     *
     * @param key file key must not be null
     */
    boolean delete(@NonNull String key);

}
