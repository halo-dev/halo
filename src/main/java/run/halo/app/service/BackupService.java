package run.halo.app.service;

import com.alibaba.fastjson.JSONObject;
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
     * Import markdown content.
     *
     * @param file file
     * @return base post detail dto
     * @throws IOException throws IOException
     */
    BasePostDetailDTO importMarkdown(MultipartFile file) throws IOException;

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


    /**
     * Export all database's data.
     *
     * @return data
     */
    @NonNull
    JSONObject exportData();

    /**
     * Import data
     *
     * @param file file
     * @throws IOException throws IOException
     */
    void importData(MultipartFile file) throws IOException;
}
