package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.model.dto.post.BasePostDetailDTO;
import run.halo.app.service.BackupService;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Backup controller
 *
 * @author johnniang
 * @date 19-4-26
 */
@RestController
@RequestMapping("/api/admin/backups")
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @PostMapping("import/markdowns")
    @ApiOperation("Import markdowns")
    public List<BasePostDetailDTO> backupMarkdowns(@RequestPart("files") MultipartFile[] files) throws IOException {
        List<BasePostDetailDTO> result = new LinkedList<>();
        for (MultipartFile file : files) {
            BasePostDetailDTO post = backupService.importMarkdowns(file);
            result.add(post);
        }
        return result;
    }
}
