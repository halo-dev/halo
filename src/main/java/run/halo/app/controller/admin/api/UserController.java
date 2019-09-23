package run.halo.app.controller.admin.api;

import org.springframework.web.bind.annotation.*;
import run.halo.app.model.dto.UserDTO;
import run.halo.app.model.entity.User;
import run.halo.app.model.params.PasswordParam;
import run.halo.app.model.params.UserParam;
import run.halo.app.model.support.BaseResponse;
import run.halo.app.model.support.UpdateCheck;
import run.halo.app.service.UserService;
import run.halo.app.utils.ValidationUtils;

import javax.validation.Valid;

/**
 * User controller.
 *
 * @author johnniang
 * @date 3/19/19
 */
@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("profiles")
    public UserDTO getProfile(User user) {
        return new UserDTO().convertFrom(user);
    }

    @PutMapping("profiles")
    public UserDTO updateProfile(@RequestBody UserParam userParam, User user) {
        // Validate the user param
        ValidationUtils.validate(userParam, UpdateCheck.class);

        // Update properties
        userParam.update(user);

        // Update user and convert to dto
        return new UserDTO().convertFrom(userService.update(user));
    }

    @PutMapping("profiles/password")
    public BaseResponse updatePassword(@RequestBody @Valid PasswordParam passwordParam, User user) {
        userService.updatePassword(passwordParam.getOldPassword(), passwordParam.getNewPassword(), user.getId());
        return BaseResponse.ok("密码修改成功");
    }
}
