package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.annotation.DisableOnCondition;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.model.dto.BackupDTO;
import run.halo.app.model.dto.post.BasePostDetailDTO;
import run.halo.app.model.params.PostMarkdownParam;
import run.halo.app.service.BackupService;

/**
 * Backup controller
 *
 * @author johnniang
 * @author ryanwang
 * @author Raremaa
 * @date 2019-04-26
 */
@RestController
@RequestMapping("/api/admin/backups")
@Slf4j
public class BackupController {

    private final BackupService backupService;

    private final HaloProperties haloProperties;

    public BackupController(BackupService backupService, HaloProperties haloProperties) {
        this.backupService = backupService;
        this.haloProperties = haloProperties;
    }

    @PostMapping("work-dir")
    @ApiOperation("Backups work directory")
    @DisableOnCondition
    public BackupDTO backupHalo() {
        return backupService.backupWorkDirectory();
    }

    @GetMapping("work-dir")
    @ApiOperation("Gets all work directory backups")
    public List<BackupDTO> listBackups() {
        return backupService.listWorkDirBackups();
    }

    @GetMapping("work-dir/{fileName:.+}")
    @ApiOperation("Downloads a work directory backup file")
    @DisableOnCondition
    public ResponseEntity<Resource> downloadBackup(@PathVariable("fileName") String fileName,
        HttpServletRequest request) {
        log.info("Try to download backup file: [{}]", fileName);

        // Load file as resource
        Resource backupResource =
            backupService.loadFileAsResource(haloProperties.getBackupDir(), fileName);

        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        // Try to determine file's content type
        try {
            contentType =
                request.getServletContext().getMimeType(backupResource.getFile().getAbsolutePath());
        } catch (IOException e) {
            log.warn("Could not determine file type", e);
            // Ignore this error
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + backupResource.getFilename() + "\"")
            .body(backupResource);
    }

    @DeleteMapping("work-dir")
    @ApiOperation("Deletes a work directory backup")
    @DisableOnCondition
    public void deleteBackup(@RequestParam("filename") String filename) {
        backupService.deleteWorkDirBackup(filename);
    }

    @PostMapping("markdown/import")
    @ApiOperation("Imports markdown")
    public BasePostDetailDTO backupMarkdowns(@RequestPart("file") MultipartFile file)
        throws IOException {
        return backupService.importMarkdown(file);
    }

    @PostMapping("data")
    @ApiOperation("Exports all data")
    @DisableOnCondition
    public BackupDTO exportData() {
        return backupService.exportData();
    }

    @GetMapping("data")
    @ApiOperation("Lists all exported data")
    public List<BackupDTO> listExportedData() {
        return backupService.listExportedData();
    }

    @DeleteMapping("data")
    @ApiOperation("Deletes a exported data")
    @DisableOnCondition
    public void deleteExportedData(@RequestParam("filename") String filename) {
        backupService.deleteExportedData(filename);
    }

    @GetMapping("data/{fileName:.+}")
    @ApiOperation("Downloads a exported data")
    @DisableOnCondition
    public ResponseEntity<Resource> downloadExportedData(@PathVariable("fileName") String fileName,
        HttpServletRequest request) {
        log.info("Try to download exported data file: [{}]", fileName);

        // Load exported data as resource
        Resource exportDataResource =
            backupService.loadFileAsResource(haloProperties.getDataExportDir(), fileName);

        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        // Try to determine file's content type
        try {
            contentType = request.getServletContext()
                .getMimeType(exportDataResource.getFile().getAbsolutePath());
        } catch (IOException e) {
            log.warn("Could not determine file type", e);
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + exportDataResource.getFilename() + "\"")
            .body(exportDataResource);
    }

    @PostMapping("markdown/export")
    @ApiOperation("Exports markdowns")
    @DisableOnCondition
    public BackupDTO exportMarkdowns(@RequestBody PostMarkdownParam postMarkdownParam)
        throws IOException {
        return backupService.exportMarkdowns(postMarkdownParam);
    }

    @GetMapping("markdown/export")
    @ApiOperation("Gets all markdown backups")
    public List<BackupDTO> listMarkdowns() {
        return backupService.listMarkdowns();
    }

    @DeleteMapping("markdown/export")
    @ApiOperation("Deletes a markdown backup")
    @DisableOnCondition
    public void deleteMarkdown(@RequestParam("filename") String filename) {
        backupService.deleteMarkdown(filename);
    }

    @GetMapping("markdown/export/{fileName:.+}")
    @ApiOperation("Downloads a work markdown backup file")
    @DisableOnCondition
    public ResponseEntity<Resource> downloadMarkdown(@PathVariable("fileName") String fileName,
        HttpServletRequest request) {
        log.info("Try to download markdown backup file: [{}]", fileName);

        // Load file as resource
        Resource backupResource =
            backupService.loadFileAsResource(haloProperties.getBackupMarkdownDir(), fileName);

        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        // Try to determine file's content type
        try {
            contentType =
                request.getServletContext().getMimeType(backupResource.getFile().getAbsolutePath());
        } catch (IOException e) {
            log.warn("Could not determine file type", e);
            // Ignore this error
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + backupResource.getFilename() + "\"")
            .body(backupResource);
    }


}
