package run.halo.app.controller.admin.api;

import cn.hutool.core.util.ZipUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.exception.FileOperationException;
import run.halo.app.model.dto.post.BasePostDetailDTO;
import run.halo.app.service.BackupService;

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
 * @date 19-4-26
 */
@RestController
@RequestMapping("/api/admin/backups")
@Slf4j
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
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
