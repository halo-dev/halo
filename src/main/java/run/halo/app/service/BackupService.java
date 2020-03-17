package run.halo.app.service;

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
 * @author ryanwang
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
    BackupDTO backupWorkDirectory();


    /**
     * Lists all backups.
     *
     * @return backup list
     */
    @NonNull
    List<BackupDTO> listWorkDirBackups();

    /**
     * Deletes backup.
     *
     * @param fileName filename must not be blank
     */
    void deleteWorkDirBackup(@NonNull String fileName);

    /**
     * Loads file as resource.
     *
     * @param fileName backup file name must not be blank.
     * @param basePath base path
     * @return resource of the given file
     */
    @NonNull
    Resource loadFileAsResource(@NonNull String basePath, @NonNull String fileName);


    /**
     * Export all database's data.
     *
     * @return data
     */
    @NonNull
    BackupDTO exportData();

    /**
     * List all exported data.
     *
     * @return list of backup dto
     */
    List<BackupDTO> listExportedData();

    /**
     * Deletes exported data.
     *
     * @param fileName fileName
     */
    void deleteExportedData(@NonNull String fileName);

    /**
     * Import data
     *
     * @param file file
     * @throws IOException throws IOException
     */
    void importData(MultipartFile file) throws IOException;
}
