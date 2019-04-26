package run.halo.app.controller.admin.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.service.RecoveryService;

/**
 * Recovery controller
 *
 * @author johnniang
 * @date 19-4-26
 */
@RestController
@RequestMapping("/api/admin/recoveries")
public class RecoveryController {

    private final RecoveryService recoveryService;

    public RecoveryController(RecoveryService recoveryService) {
        this.recoveryService = recoveryService;
    }
}
