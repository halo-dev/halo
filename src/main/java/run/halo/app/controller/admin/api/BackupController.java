package run.halo.app.controller.admin.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.service.BackupService;

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
}
