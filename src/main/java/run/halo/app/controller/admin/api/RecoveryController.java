package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
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

    @PostMapping
    @ApiOperation("Migrate from halo v0.4.3")
    public void migrateFromVersion_0_4_3(
            @ApiParam("This file content type should be json")
            @RequestPart("file") MultipartFile file) {
        recoveryService.migrateFromV0_4_3(file);
    }
}
