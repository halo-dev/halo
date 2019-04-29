package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import run.halo.app.cache.lock.CacheLock;
import run.halo.app.exception.BadRequestException;
import run.halo.app.model.dto.CountDTO;
import run.halo.app.model.params.LoginParam;
import run.halo.app.security.context.SecurityContextHolder;
import run.halo.app.security.filter.AdminAuthenticationFilter;
import run.halo.app.security.token.AuthToken;
import run.halo.app.service.AdminService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * Admin controller.
 *
 * @author johnniang
 * @date 3/19/19
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Get some statistics about the count of posts, the count of comments, etc.
     *
     * @return counts
     */
    @GetMapping("counts")
    @ApiOperation("Gets count info")
    public CountDTO getCount() {
        return adminService.getCount();
    }

    @PostMapping("auth/login")
    @ApiOperation("Login")
    public AuthToken auth(@RequestBody @Valid LoginParam loginParam) {
        return adminService.authenticate(loginParam);
    }

    @PostMapping("logout")
    @ApiOperation("Logs out (Clear session)")
    @CacheLock
    public void logout(HttpServletRequest request) {
        adminService.clearAuthentication();
        // Check if the current is logging in
        boolean authenticated = SecurityContextHolder.getContext().isAuthenticated();

        if (!authenticated) {
            throw new BadRequestException("You haven't logged in yet, so you can't log out");
        }

        request.getSession().removeAttribute(AdminAuthenticationFilter.ADMIN_SESSION_KEY);

        log.info("You have been logged out, Welcome to you next time!");
    }
}
