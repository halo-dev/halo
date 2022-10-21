package run.halo.app.controller.admin.api;

import static run.halo.app.service.BackupService.BackupType.JSON_DATA;
import static run.halo.app.service.BackupService.BackupType.MARKDOWN;
import static run.halo.app.service.BackupService.BackupType.WHOLE_SITE;

import com.google.common.collect.Lists;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
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
import run.halo.app.exception.BadRequestException;
import run.halo.app.exception.NotFoundException;
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

    @GetMapping("work-dir/fetch")
    public BackupDTO getWorkDirBackup(@RequestParam("filename") String filename) {
        return backupService
            .getBackup(Paths.get(haloProperties.getBackupDir(), filename), WHOLE_SITE)
            .orElseThrow(() ->
                new NotFoundException("备份文件 " + filename + " 不存在或已删除！").setErrorData(filename));
    }

    @GetMapping("data/fetch")
    public BackupDTO getDataBackup(@RequestParam("filename") String filename) {
        return backupService
            .getBackup(Paths.get(haloProperties.getDataExportDir(), filename), JSON_DATA)
            .orElseThrow(() ->
                new NotFoundException("备份文件 " + filename + " 不存在或已删除！").setErrorData(filename));
    }

    @GetMapping("markdown/fetch")
    public BackupDTO getMarkdownBackup(@RequestParam("filename") String filename) {
        return backupService
            .getBackup(Paths.get(haloProperties.getBackupMarkdownDir(), filename), MARKDOWN)
            .orElseThrow(() ->
                new NotFoundException("备份文件 " + filename + " 不存在或已删除！").setErrorData(filename));
    }

    @PostMapping("work-dir")
    @ApiOperation("Backups work directory")
    @DisableOnCondition
    public BackupDTO backupHalo(@RequestBody List<String> options) {
        return backupService.backupWorkDirectory(options);
    }

    @GetMapping("work-dir/options")
    @ApiOperation("Gets items that can be backed up")
    public List<String> listBackupItems() throws IOException {
        return Files.list(Paths.get(haloProperties.getWorkDir()))
            .map(Path::getFileName)
            .filter(Objects::nonNull)
            .map(Path::toString)
            .sorted()
            .collect(Collectors.toList());
    }

    @GetMapping("work-dir")
    @ApiOperation("Gets all work directory backups")
    public List<BackupDTO> listBackups() {
        return backupService.listWorkDirBackups();
    }

    @GetMapping("work-dir/{filename:.+}")
    @ApiOperation("Downloads a work directory backup file")
    @DisableOnCondition
    public ResponseEntity<Resource> downloadBackup(@PathVariable("filename") String filename,
        HttpServletRequest request) {
        log.info("Trying to download backup file: [{}]", filename);

        // Load file as resource
        Resource backupResource =
            backupService.loadFileAsResource(haloProperties.getBackupDir(), filename);

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

    @PostMapping(value = "markdown/import")
    @ApiOperation("Imports markdown")
    public BasePostDetailDTO backupMarkdowns(@RequestPart("file") MultipartFile file)
        throws IOException {
        List<String> supportType = Lists.newArrayList("md", "markdown", "mdown");
        String filename = file.getOriginalFilename();
        if (StringUtils.isEmpty(filename)) {
            throw new BadRequestException("文件名不可为空").setErrorData(filename);
        }
        String extension = FilenameUtils.getExtension(filename).toLowerCase();
        if (!supportType.contains(extension)) {
            throw new BadRequestException(
                "不支持" + (StringUtils.isNotEmpty(extension) ? extension : "未知") 
                    + "格式的文件上传").setErrorData(filename);
        }
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
