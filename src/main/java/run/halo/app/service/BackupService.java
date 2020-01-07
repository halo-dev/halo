package run.halo.app.service;

import org.json.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.model.dto.BackupDTO;
import run.halo.app.model.dto.post.BasePostDetailDTO;

import java.io.IOException;
import java.util.List;

/**
 * Backup service interface.
 *
 * @author johnniang
 * @date 2019-04-26
 */
public interface BackupService {

    /**
     * Backup posts and sheets
     *
     * @param file file
     * @return post info
     */
    BasePostDetailDTO importMarkdown(MultipartFile file) throws IOException;


    /**
     * export posts by hexo formatter
     *
     * @return json object
     */
    JSONObject exportHexoMDs();

    /**
     * Exports the specified articles to the specified dir path.
     *
     * @param posts
     * @param path
     */
    void exportHexoMd(List<JSONObject> posts, String path);

    /**
     * Zips work directory.
     *
     * @return backup dto.
     */
    @NonNull
    BackupDTO zipWorkDirectory();


    /**
     * Lists all backups.
     *
     * @return backup list
     */
    @NonNull
    List<BackupDTO> listHaloBackups();

    /**
     * Deletes backup.
     *
     * @param fileName filename must not be blank
     */
    void deleteHaloBackup(@NonNull String fileName);

    /**
     * Loads file as resource.
     *
     * @param fileName backup file name must not be blank.
     * @return resource of the given file
     */
    @NonNull
    Resource loadFileAsResource(@NonNull String fileName);
}
