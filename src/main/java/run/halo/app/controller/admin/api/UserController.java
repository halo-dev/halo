package run.halo.app.controller.admin.api;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import run.halo.app.exception.BadRequestException;
import run.halo.app.model.annotation.DisableOnCondition;
import run.halo.app.model.dto.UserDTO;
import run.halo.app.model.entity.User;
import run.halo.app.model.enums.MFAType;
import run.halo.app.model.params.LoginParam;
import run.halo.app.model.params.PasswordParam;
import run.halo.app.model.params.MultiFactorAuthParam;
import run.halo.app.model.params.UserParam;
import run.halo.app.model.support.BaseResponse;
import run.halo.app.model.support.UpdateCheck;
import run.halo.app.model.vo.MultiFactorAuthVO;
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

    @GetMapping("mfa/generate")
    @ApiOperation("Generate Multi-Factor Auth qr image")
    @DisableOnCondition
    public MultiFactorAuthVO generateMFAQrImage(@RequestBody @Valid MultiFactorAuthParam multiFactorAuthParam, User user) {
        if (MFAType.NONE == user.getMfaType()) {
            if (MFAType.TFA_TOTP == multiFactorAuthParam.getMfaType()) {
                String mfaKey = TwoFactorAuthUtils.generateTFAKey();
                String optAuthUrl = TwoFactorAuthUtils.generateOtpAuthUrl(user.getNickname(), mfaKey);
                String qrImageBase64 = Base64.encode(QrCodeUtil.generatePng(optAuthUrl, 256, 256));
                return new MultiFactorAuthVO("image/png;base64," + qrImageBase64, optAuthUrl, mfaKey);
            } else {
                throw new BadRequestException("暂不支持的MFA认证的方式");
            }
        } else {
            throw new BadRequestException("MFA认证已启用，无需重复操作");
        }
    }

    @PutMapping("mfa/update")
    @ApiOperation("Updates user's Multi Factor Auth")
    @DisableOnCondition
    public BaseResponse<String> updateMFAuth(@RequestBody @Valid MultiFactorAuthParam multiFactorAuthParam, User user) {
        if ((StrUtil.isNotBlank(user.getMfaKey()) && multiFactorAuthParam.useMFA())) {
            return BaseResponse.ok("两步验证已是开启状态，不需要再次开启");
        } else if (StrUtil.isBlank(user.getMfaKey()) && !multiFactorAuthParam.useMFA()) {
            return BaseResponse.ok("两步验证已是关闭状态，不需要再次关闭");
        } else {
            final String tfaKey = StrUtil.isNotBlank(user.getMfaKey()) ? user.getMfaKey() : multiFactorAuthParam.getMfaKey();
            TwoFactorAuthUtils.validateTFACode(tfaKey, multiFactorAuthParam.getAuthCode());
        }
        // 保存两步验证
        userService.updateMFA(multiFactorAuthParam.getMfaType(), multiFactorAuthParam.getMfaKey(), user.getId());

        String tips = multiFactorAuthParam.useMFA() ? "两步验证已启用,下次登陆生效!" : "两步验证已关闭";
        return BaseResponse.ok(tips);
    }

    @PutMapping("mfa/check")
    @ApiOperation("check user's Multi-Factor Auth")
    @DisableOnCondition
    public BaseResponse<String> checkMFAuth(@RequestBody @Valid MultiFactorAuthParam multiFactorAuthParam, User user) {
        if (!MFAType.useMFA(user.getMfaType())) {
            return BaseResponse.ok("未开启MFA认证");
        }
        TwoFactorAuthUtils.validateTFACode(user.getMfaKey(), multiFactorAuthParam.getAuthCode());
        return BaseResponse.ok("check success!");
    }
}
