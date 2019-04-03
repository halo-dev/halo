package run.halo.app.web.controller.admin.api;

import run.halo.app.model.dto.UserOutputDTO;
import run.halo.app.model.entity.User;
import run.halo.app.model.params.PasswordParam;
import run.halo.app.model.params.UserParam;
import run.halo.app.service.UserService;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.dto.UserOutputDTO;
import run.halo.app.model.entity.User;
import run.halo.app.model.params.PasswordParam;
import run.halo.app.model.params.UserParam;

import javax.validation.Valid;

/**
 * User controller.
 *
 * @author johnniang
 * @date 3/19/19
 */
@RestController
@RequestMapping("/admin/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("profile")
    public UserOutputDTO getProfile(User user) {
        return new UserOutputDTO().convertFrom(user);
    }

    @PutMapping("profile")
    public UserOutputDTO updateProfile(@Valid @RequestBody UserParam userParam, User user) {
        // Update properties
        userParam.update(user);

        // Update user and convert to dto
        return new UserOutputDTO().convertFrom(userService.update(user));
    }

    @PutMapping("profile/password")
    public void updatePassword(@Valid @RequestBody PasswordParam passwordParam, User user) {
        userService.updatePassword(passwordParam.getOldPassword(), passwordParam.getNewPassword(), user.getId());
    }
}
