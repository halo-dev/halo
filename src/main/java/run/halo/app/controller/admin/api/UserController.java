package run.halo.app.controller.admin.api;

import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.annotation.DisableOnCondition;
import run.halo.app.model.dto.UserDTO;
import run.halo.app.model.entity.User;
import run.halo.app.model.params.PasswordParam;
import run.halo.app.model.params.TwoFactorAuthParam;
import run.halo.app.model.params.UserParam;
import run.halo.app.model.support.BaseResponse;
import run.halo.app.model.support.UpdateCheck;
import run.halo.app.service.UserService;
import run.halo.app.utils.TwoFactorAuthUtils;
import run.halo.app.utils.ValidationUtils;

import javax.validation.Valid;

/**
 * User controller.
 *
 * @author johnniang
 * @date 2019-03-19
 */
@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("profiles")
    @ApiOperation("Gets user profile")
    public UserDTO getProfile(User user) {
        return new UserDTO().convertFrom(user);
    }

    @PutMapping("profiles")
    @ApiOperation("Updates user profile")
    @DisableOnCondition
    public UserDTO updateProfile(@RequestBody UserParam userParam, User user) {
        // Validate the user param
        ValidationUtils.validate(userParam, UpdateCheck.class);

        // Update properties
        userParam.update(user);

        // Update user and convert to dto
        return new UserDTO().convertFrom(userService.update(user));
    }

    @PutMapping("profiles/password")
    @ApiOperation("Updates user's password")
    @DisableOnCondition
    public BaseResponse<String> updatePassword(@RequestBody @Valid PasswordParam passwordParam, User user) {
        userService.updatePassword(passwordParam.getOldPassword(), passwordParam.getNewPassword(), user.getId());
        return BaseResponse.ok("密码修改成功");
    }

    @PutMapping("profiles/tfa")
    @ApiOperation("use two factor auth")
    @DisableOnCondition
    public BaseResponse<String> updateTwoFactorAuth(@RequestBody @Valid TwoFactorAuthParam twoFactorAuthParam, User user) {
        if ((StrUtil.isNotBlank(user.getTfaKey()) && twoFactorAuthParam.isOpen())) {
            return BaseResponse.ok("两步验证已是开启状态，不需要再次开启");
        } else if (StrUtil.isBlank(user.getTfaKey()) && !twoFactorAuthParam.isOpen()) {
            return BaseResponse.ok("两步验证已是关闭状态，不需要再次关闭");
        } else {
            final String tfaKey = StrUtil.isNotBlank(user.getTfaKey()) ? user.getTfaKey() : twoFactorAuthParam.getTfaKey();
            TwoFactorAuthUtils.validateTFACode(tfaKey, twoFactorAuthParam.getTfaCode());
        }
        // 保存两步验证
        userService.setTwoFactorAuth(twoFactorAuthParam.isOpen(), twoFactorAuthParam.getTfaKey(), user.getId());
        String tips = twoFactorAuthParam.isOpen() ? "两步验证已启用,下次登陆生效!" : "两步验证已关闭";
        return BaseResponse.ok(tips);
    }
}
