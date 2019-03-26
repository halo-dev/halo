package cc.ryanc.halo.service;

import cc.ryanc.halo.model.support.UploadResult;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

/**
 * File service interface.
 *
 * @author johnniang
 * @date 3/26/19
 */
public interface FileService {

    /**
     * Upload sub directory.
     */
    String UPLOAD_SUB_DIR = "upload";

    /**
     * Thumbnail width.
     */
    int THUMB_WIDTH = 256;

    /**
     * Thumbnail height.
     */
    int THUMB_HEIGHT = 256;

    /**
     * Uploads file to local storage.
     *
     * @param file multipart file must not be null
     * @return upload result
     */
    @NonNull
    UploadResult uploadToLocal(@NonNull MultipartFile file);

    /**
     * Uploads file to qi niu yun.
     *
     * @param file multipart file must not be null
     * @return upload result
     */
    @NonNull
    UploadResult uploadToQnYun(@NonNull MultipartFile file);

    /**
     * Uploads file to you pai yun.
     *
     * @param file multipart file must not be null
     * @return upload result
     */
    @NonNull
    UploadResult uploadToYpYun(@NonNull MultipartFile file);
}
