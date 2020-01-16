package run.halo.app.service;

import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.model.support.StaticFile;

import java.util.List;

/**
 * Static storage service interface class.
 *
 * @author ryanwang
 * @date 2019-12-06
 */
public interface StaticStorageService {

    /**
     * Static folder location.
     */
    String STATIC_FOLDER = "static";

    /**
     * Lists static folder.
     *
     * @return List<StaticFile>
     */
    List<StaticFile> listStaticFolder();

    /**
     * Delete file or folder by relative path
     *
     * @param relativePath relative path
     */
    void delete(@NonNull String relativePath);

    /**
     * Create folder.
     *
     * @param basePath   base path
     * @param folderName folder name must not be null
     */
    void createFolder(String basePath, @NonNull String folderName);

    /**
     * Update static file.
     *
     * @param basePath base path
     * @param file     file must not be null.
     */
    void update(String basePath, @NonNull MultipartFile file);
}
