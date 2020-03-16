package run.halo.app.controller.admin.api;

import cn.hutool.core.date.DateUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.model.annotation.DisableOnCondition;
import run.halo.app.model.dto.BackupDTO;
import run.halo.app.model.dto.post.BasePostDetailDTO;
import run.halo.app.service.BackupService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Backup controller
 *
 * @author johnniang
 * @date 2019-04-26
 */
@RestController
@RequestMapping("/api/admin/backups")
@Slf4j
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @PostMapping("halo")
    @ApiOperation("Backups halo")
    @DisableOnCondition
    public BackupDTO backupHalo() {
        return backupService.zipWorkDirectory();
    }

    @GetMapping("halo")
    @ApiOperation("Gets all backups")
    public List<BackupDTO> listBackups() {
        return backupService.listHaloBackups();
    }

    @GetMapping("halo/{fileName:.+}")
    @ApiOperation("Downloads backup file")
    @DisableOnCondition
    public ResponseEntity<Resource> downloadBackup(@PathVariable("fileName") String fileName, HttpServletRequest request) {
        log.info("Try to download backup file: [{}]", fileName);

        // Load file as resource
        Resource backupResource = backupService.loadFileAsResource(fileName);

        String contentType = "application/octet-stream";
        // Try to determine file's content type
        try {
            contentType = request.getServletContext().getMimeType(backupResource.getFile().getAbsolutePath());
        } catch (IOException e) {
            log.warn("Could not determine file type", e);
            // Ignore this error
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + backupResource.getFilename() + "\"")
            .body(backupResource);
    }

    @DeleteMapping("halo")
    @ApiOperation("Deletes a backup")
    @DisableOnCondition
    public void deleteBackup(@RequestParam("filename") String filename) {
        backupService.deleteHaloBackup(filename);
    }

    @PostMapping("import/markdown")
    @ApiOperation("Import markdown")
    public BasePostDetailDTO backupMarkdowns(@RequestPart("file") MultipartFile file) throws IOException {
        return backupService.importMarkdown(file);
    }

    @GetMapping("export/data")
    @DisableOnCondition
    public ResponseEntity<String> exportData() {

        String contentType = "application/octet-stream;charset=UTF-8";

        String filename = "halo-data-" + DateUtil.format(new Date(), "yyyy-MM-dd-HH-mm-ss.json");

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .body(backupService.exportData().toJSONString());
    }
}
