package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import run.halo.app.cache.lock.CacheLock;
import run.halo.app.model.dto.EnvironmentDTO;
import run.halo.app.model.dto.StatisticDTO;
import run.halo.app.model.params.LoginParam;
import run.halo.app.model.params.ResetPasswordParam;
import run.halo.app.model.properties.PrimaryProperties;
import run.halo.app.model.support.BaseResponse;
import run.halo.app.security.token.AuthToken;
import run.halo.app.service.AdminService;
import run.halo.app.service.OptionService;

import javax.validation.Valid;

/**
 * Admin controller.
 *
 * @author johnniang
 * @author ryanwang
 * @date 3/19/19
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    private final OptionService optionService;

    public AdminController(AdminService adminService, OptionService optionService) {
        this.adminService = adminService;
        this.optionService = optionService;
    }

    @GetMapping(value = "/is_installed")
    @ApiOperation("Check install status")
    public boolean isInstall() {
        return optionService.getByPropertyOrDefault(PrimaryProperties.IS_INSTALLED, Boolean.class, false);
    }

    @PostMapping("login")
    @ApiOperation("Login")
    @CacheLock(autoDelete = false)
    public AuthToken auth(@RequestBody @Valid LoginParam loginParam) {
        return adminService.authenticate(loginParam);
    }

    @PostMapping("logout")
    @ApiOperation("Logs out (Clear session)")
    @CacheLock(autoDelete = false)
    public void logout() {
        adminService.clearToken();
    }

    @PostMapping("password/code")
    @ApiOperation("Send reset password verify code.")
    public void sendResetCode(@RequestBody @Valid ResetPasswordParam param) {
        adminService.sendResetPasswordCode(param);
    }

    @PutMapping("password/reset")
    @ApiOperation("Reset password by verify code.")
    public void resetPassword(@RequestBody @Valid ResetPasswordParam param) {
        adminService.resetPasswordByCode(param);
    }

    @PostMapping("refresh/{refreshToken}")
    @ApiOperation("Refreshes token")
    @CacheLock(autoDelete = false)
    public AuthToken refresh(@PathVariable("refreshToken") String refreshToken) {
        return adminService.refreshToken(refreshToken);
    }

    /**
     * Get some statistics about the count of posts, the count of comments, etc.
     *
     * @return counts
     */
    @GetMapping("counts")
    @ApiOperation("Gets count info")
    public StatisticDTO getCount() {
        return adminService.getCount();
    }

    @GetMapping("environments")
    @ApiOperation("Gets environments info")
    public EnvironmentDTO getEnvironments() {
        return adminService.getEnvironments();
    }

    @PutMapping("halo-admin")
    @ApiOperation("Updates halo-admin manually")
    public void updateAdmin() {
        adminService.updateAdminAssets();
    }

    @GetMapping("spring/logs")
    @ApiOperation("Get application logs")
    public BaseResponse<String> getSpringLogs() {
        return BaseResponse.ok(HttpStatus.OK.getReasonPhrase(), adminService.getSpringLogs());
    }
}
