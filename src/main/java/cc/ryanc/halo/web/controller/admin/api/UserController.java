package cc.ryanc.halo.web.controller.admin.api;

import cc.ryanc.halo.model.dto.UserOutputDTO;
import cc.ryanc.halo.model.entity.User;
import cc.ryanc.halo.model.params.PasswordParam;
import cc.ryanc.halo.model.params.UserParam;
import cc.ryanc.halo.service.UserService;
import org.springframework.web.bind.annotation.*;

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
