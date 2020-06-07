package run.halo.app.controller.admin.api;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import run.halo.app.annotation.DisableOnCondition;
import run.halo.app.cache.lock.CacheLock;
import run.halo.app.exception.BadRequestException;
import run.halo.app.model.dto.UserDTO;
import run.halo.app.model.entity.User;
import run.halo.app.model.enums.MFAType;
import run.halo.app.model.params.MultiFactorAuthParam;
import run.halo.app.model.params.PasswordParam;
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

    @PutMapping("mfa/generate")
    @ApiOperation("Generate Multi-Factor Auth qr image")
    @DisableOnCondition
    public MultiFactorAuthVO generateMFAQrImage(@RequestBody MultiFactorAuthParam multiFactorAuthParam, User user) {
        if (MFAType.NONE == user.getMfaType()) {
            if (MFAType.TFA_TOTP == multiFactorAuthParam.getMfaType()) {
                String mfaKey = TwoFactorAuthUtils.generateTFAKey();
                String optAuthUrl = TwoFactorAuthUtils.generateOtpAuthUrl(user.getNickname(), mfaKey);
                String qrImageBase64 = "data:image/png;base64," +
                    Base64.encode(QrCodeUtil.generatePng(optAuthUrl, 128, 128));
                return new MultiFactorAuthVO(qrImageBase64, optAuthUrl, mfaKey, MFAType.TFA_TOTP);
            } else {
                throw new BadRequestException("暂不支持的 MFA 认证的方式");
            }
        } else {
            throw new BadRequestException("MFA 认证已启用，无需重复操作");
        }
    }

    @PutMapping("mfa/update")
    @ApiOperation("Updates user's Multi Factor Auth")
    @CacheLock(autoDelete = false, prefix = "mfa")
    @DisableOnCondition
    public MultiFactorAuthVO updateMFAuth(@RequestBody @Valid MultiFactorAuthParam multiFactorAuthParam, User user) {
        if (StrUtil.isNotBlank(user.getMfaKey()) && MFAType.useMFA(multiFactorAuthParam.getMfaType())) {
            return new MultiFactorAuthVO(MFAType.TFA_TOTP);
        } else if (StrUtil.isBlank(user.getMfaKey()) && !MFAType.useMFA(multiFactorAuthParam.getMfaType())) {
            return new MultiFactorAuthVO(MFAType.NONE);
        } else {
            final String tfaKey = StrUtil.isNotBlank(user.getMfaKey()) ? user.getMfaKey() : multiFactorAuthParam.getMfaKey();
            TwoFactorAuthUtils.validateTFACode(tfaKey, multiFactorAuthParam.getAuthcode());
        }
        // update MFA key
        User updateUser = userService.updateMFA(multiFactorAuthParam.getMfaType(), multiFactorAuthParam.getMfaKey(), user.getId());

        return new MultiFactorAuthVO(updateUser.getMfaType());
    }
}
