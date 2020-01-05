package run.halo.app.controller.admin.api;

import cn.hutool.core.util.ZipUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.json.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.exception.FileOperationException;
import run.halo.app.model.dto.BackupDTO;
import run.halo.app.model.dto.post.BasePostDetailDTO;
import run.halo.app.service.BackupService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
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
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + backupResource.getFilename() + "\"")
                .body(backupResource);
    }

    @DeleteMapping("halo")
    @ApiOperation("Deletes a backup")
    public void deleteBackup(@RequestParam("filename") String filename) {
        backupService.deleteHaloBackup(filename);
    }

    @PostMapping("import/markdown")
    @ApiOperation("Import markdown")
    public BasePostDetailDTO backupMarkdowns(@RequestPart("file") MultipartFile file) throws IOException {
        return backupService.importMarkdown(file);
    }

    @GetMapping("export/hexo")
    @ApiOperation("export hexo markdown")
    public void exportMarkdowns(HttpServletResponse response) {
        final String tmpDir = System.getProperty("java.io.tmpdir");
        final String date = DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
        String localFilePath = tmpDir + File.separator + "halo-markdown-" + date;
        log.trace(localFilePath);
        final File localFile = new File(localFilePath);
        final File postDir = new File(localFilePath + File.separator + "posts");
        final File passwordDir = new File(localFilePath + File.separator + "passwords");
        final File draftDir = new File(localFilePath + File.separator + "drafts");
        try {
            if (!postDir.mkdirs()) {
                throw new Exception("Create dir [" + postDir.getPath() + "] failed");
            }
            if (!passwordDir.mkdirs()) {
                throw new Exception("Create dir [" + passwordDir.getPath() + "] failed");
            }
            if (!draftDir.mkdirs()) {
                throw new Exception("Create dir [" + draftDir.getPath() + "] failed");
            }
            final JSONObject result = backupService.exportHexoMDs();
            final List<JSONObject> posts = (List<JSONObject>) result.opt("posts");
            backupService.exportHexoMd(posts, postDir.getPath());
            final List<JSONObject> passwords = (List<JSONObject>) result.opt("passwords");
            backupService.exportHexoMd(passwords, passwordDir.getPath());
            final List<JSONObject> drafts = (List<JSONObject>) result.opt("drafts");
            backupService.exportHexoMd(drafts, draftDir.getPath());

            final File zipFile = ZipUtil.zip(localFile);
            byte[] zipData;
            try (final FileInputStream inputStream = new FileInputStream(zipFile)) {
                zipData = IOUtils.toByteArray(inputStream);
                response.setContentType("application/zip");
                final String fileName = "halo-markdown-" + date + ".zip";
                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            }

            response.getOutputStream().write(zipData);
        } catch (final Exception e) {
            log.error("Export failed", e);
            throw new FileOperationException("文章导出失败", e);
        }
    }
}
