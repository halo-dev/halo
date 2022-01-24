package run.halo.app.service.impl;

import static run.halo.app.model.support.HaloConst.DATABASE_PRODUCT_NAME;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import run.halo.app.cache.AbstractStringCacheStore;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.event.logger.LogEvent;
import run.halo.app.exception.BadRequestException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.exception.ServiceException;
import run.halo.app.mail.MailService;
import run.halo.app.model.dto.EnvironmentDTO;
import run.halo.app.model.dto.LoginPreCheckDTO;
import run.halo.app.model.entity.User;
import run.halo.app.model.enums.LogType;
import run.halo.app.model.enums.MFAType;
import run.halo.app.model.params.LoginParam;
import run.halo.app.model.params.ResetPasswordParam;
import run.halo.app.model.params.ResetPasswordSendCodeParam;
import run.halo.app.model.properties.EmailProperties;
import run.halo.app.model.support.HaloConst;
import run.halo.app.security.authentication.Authentication;
import run.halo.app.security.context.SecurityContextHolder;
import run.halo.app.security.token.AuthToken;
import run.halo.app.security.util.SecurityUtils;
import run.halo.app.service.AdminService;
import run.halo.app.service.OptionService;
import run.halo.app.service.UserService;
import run.halo.app.utils.HaloUtils;
import run.halo.app.utils.TwoFactorAuthUtils;
import run.halo.app.utils.ValidationUtils;

/**
 * Admin service implementation.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-04-29
 */
@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    private final OptionService optionService;

    private final UserService userService;

    private final MailService mailService;

    private final AbstractStringCacheStore cacheStore;

    private final HaloProperties haloProperties;

    private final ApplicationEventPublisher eventPublisher;

    public AdminServiceImpl(
        OptionService optionService,
        UserService userService,
        MailService mailService,
        AbstractStringCacheStore cacheStore,
        HaloProperties haloProperties,
        ApplicationEventPublisher eventPublisher) {
        this.optionService = optionService;
        this.userService = userService;
        this.mailService = mailService;
        this.cacheStore = cacheStore;
        this.haloProperties = haloProperties;
        this.eventPublisher = eventPublisher;
    }


    @Override
    @NonNull
    public User authenticate(@NonNull LoginParam loginParam) {
        Assert.notNull(loginParam, "Login param must not be null");

        String username = loginParam.getUsername();

        String mismatchTip = "用户名或者密码不正确";

        final User user;

        try {
            // Get user by username or email
            user = ValidationUtils.isEmail(username)
                ? userService.getByEmailOfNonNull(username) :
                userService.getByUsernameOfNonNull(username);
        } catch (NotFoundException e) {
            log.error("Failed to find user by name: " + username);
            eventPublisher.publishEvent(
                new LogEvent(this, loginParam.getUsername(), LogType.LOGIN_FAILED,
                    loginParam.getUsername()));

            throw new BadRequestException(mismatchTip);
        }

        userService.mustNotExpire(user);

        if (!userService.passwordMatch(user, loginParam.getPassword())) {
            // If the password is mismatch
            eventPublisher.publishEvent(
                new LogEvent(this, loginParam.getUsername(), LogType.LOGIN_FAILED,
                    loginParam.getUsername()));

            throw new BadRequestException(mismatchTip);
        }

        return user;
    }

    @Override
    @NonNull
    public AuthToken authCodeCheck(@NonNull final LoginParam loginParam) {
        // get user
        final User user = this.authenticate(loginParam);

        // check authCode
        if (MFAType.useMFA(user.getMfaType())) {
            if (StringUtils.isBlank(loginParam.getAuthcode())) {
                throw new BadRequestException("请输入两步验证码");
            }
            TwoFactorAuthUtils.validateTFACode(user.getMfaKey(), loginParam.getAuthcode());
        }

        if (SecurityContextHolder.getContext().isAuthenticated()) {
            // If the user has been logged in
            throw new BadRequestException("您已登录，请不要重复登录");
        }

        // Log it then login successful
        eventPublisher.publishEvent(
            new LogEvent(this, user.getUsername(), LogType.LOGGED_IN, user.getNickname()));

        // Generate new token
        return buildAuthToken(user);
    }

    @Override
    public void clearToken() {
        // Check if the current is logging in
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new BadRequestException("您尚未登录，因此无法注销");
        }

        // Get current user
        User user = authentication.getDetail().getUser();

        // Clear access token
        cacheStore.getAny(SecurityUtils.buildAccessTokenKey(user), String.class)
            .ifPresent(accessToken -> {
                // Delete token
                cacheStore.delete(SecurityUtils.buildTokenAccessKey(accessToken));
                cacheStore.delete(SecurityUtils.buildAccessTokenKey(user));
            });

        // Clear refresh token
        cacheStore.getAny(SecurityUtils.buildRefreshTokenKey(user), String.class)
            .ifPresent(refreshToken -> {
                cacheStore.delete(SecurityUtils.buildTokenRefreshKey(refreshToken));
                cacheStore.delete(SecurityUtils.buildRefreshTokenKey(user));
            });

        eventPublisher.publishEvent(
            new LogEvent(this, user.getUsername(), LogType.LOGGED_OUT, user.getNickname()));

        log.info("You have been logged out, looking forward to your next visit!");
    }

    @Override
    public void sendResetPasswordCode(ResetPasswordSendCodeParam param) {
        cacheStore.getAny("code", String.class).ifPresent(code -> {
            throw new ServiceException("已经获取过验证码，不能重复获取");
        });

        if (!userService.verifyUser(param.getUsername(), param.getEmail())) {
            throw new ServiceException("用户名或者邮箱验证错误");
        }

        // Gets random code.
        String code = RandomStringUtils.randomNumeric(6);

        log.info("Got reset password code:{}", code);

        // Cache code.
        cacheStore.putAny("code", code, 5, TimeUnit.MINUTES);

        Boolean emailEnabled =
            optionService.getByPropertyOrDefault(EmailProperties.ENABLED, Boolean.class, false);

        if (!emailEnabled) {
            throw new ServiceException("未启用 SMTP 服务，无法发送邮件，但是你可以通过系统日志找到验证码");
        }

        // Send email to administrator.
        String content = "您正在进行密码重置操作，如不是本人操作，请尽快做好相应措施。密码重置验证码如下（五分钟有效）：\n" + code;
        mailService.sendTextMail(param.getEmail(), "找回密码验证码", content);
    }

    @Override
    public void resetPasswordByCode(ResetPasswordParam param) {
        if (StringUtils.isEmpty(param.getCode())) {
            throw new ServiceException("验证码不能为空");
        }

        if (StringUtils.isEmpty(param.getPassword())) {
            throw new ServiceException("密码不能为空");
        }

        if (!userService.verifyUser(param.getUsername(), param.getEmail())) {
            throw new ServiceException("用户名或者邮箱验证错误");
        }

        // verify code
        String code = cacheStore.getAny("code", String.class)
            .orElseThrow(() -> new ServiceException("未获取过验证码"));
        if (!code.equals(param.getCode())) {
            throw new ServiceException("验证码不正确");
        }

        User user =
            userService.getCurrentUser().orElseThrow(() -> new ServiceException("未查询到博主信息"));

        // reset password
        userService.setPassword(user, param.getPassword());

        // Update this user
        userService.update(user);

        // clear code cache
        cacheStore.delete("code");
    }

    @Override
    @NonNull
    public EnvironmentDTO getEnvironments() {
        EnvironmentDTO environmentDto = new EnvironmentDTO();

        // Get application start time.
        environmentDto.setStartTime(ManagementFactory.getRuntimeMXBean().getStartTime());

        environmentDto.setDatabase(DATABASE_PRODUCT_NAME);

        environmentDto.setVersion(HaloConst.HALO_VERSION);

        environmentDto.setMode(haloProperties.getMode());

        return environmentDto;
    }

    @Override
    @NonNull
    public AuthToken refreshToken(@NonNull String refreshToken) {
        Assert.hasText(refreshToken, "Refresh token must not be blank");

        Integer userId =
            cacheStore.getAny(SecurityUtils.buildTokenRefreshKey(refreshToken), Integer.class)
                .orElseThrow(
                    () -> new BadRequestException("登录状态已失效，请重新登录").setErrorData(refreshToken));

        // Get user info
        User user = userService.getById(userId);

        // Remove all token
        cacheStore.getAny(SecurityUtils.buildAccessTokenKey(user), String.class)
            .ifPresent(
                accessToken -> cacheStore.delete(SecurityUtils.buildTokenAccessKey(accessToken)));
        cacheStore.delete(SecurityUtils.buildTokenRefreshKey(refreshToken));
        cacheStore.delete(SecurityUtils.buildAccessTokenKey(user));
        cacheStore.delete(SecurityUtils.buildRefreshTokenKey(user));

        return buildAuthToken(user);
    }

    /**
     * Builds authentication token.
     *
     * @param user user info must not be null
     * @return authentication token
     */
    @NonNull
    private AuthToken buildAuthToken(@NonNull User user) {
        Assert.notNull(user, "User must not be null");

        // Generate new token
        AuthToken token = new AuthToken();

        token.setAccessToken(HaloUtils.randomUUIDWithoutDash());
        token.setExpiredIn(ACCESS_TOKEN_EXPIRED_SECONDS);
        token.setRefreshToken(HaloUtils.randomUUIDWithoutDash());

        // Cache those tokens, just for clearing
        cacheStore.putAny(SecurityUtils.buildAccessTokenKey(user), token.getAccessToken(),
            ACCESS_TOKEN_EXPIRED_SECONDS, TimeUnit.SECONDS);
        cacheStore.putAny(SecurityUtils.buildRefreshTokenKey(user), token.getRefreshToken(),
            REFRESH_TOKEN_EXPIRED_DAYS, TimeUnit.DAYS);

        // Cache those tokens with user id
        cacheStore.putAny(SecurityUtils.buildTokenAccessKey(token.getAccessToken()), user.getId(),
            ACCESS_TOKEN_EXPIRED_SECONDS, TimeUnit.SECONDS);
        cacheStore.putAny(SecurityUtils.buildTokenRefreshKey(token.getRefreshToken()), user.getId(),
            REFRESH_TOKEN_EXPIRED_DAYS, TimeUnit.DAYS);

        return token;
    }

    @Override
    public String getLogFiles(@NonNull Long lines) {
        Assert.notNull(lines, "Lines must not be null");

        File file = new File(haloProperties.getWorkDir(), LOG_PATH);

        List<String> linesArray = new ArrayList<>();

        final StringBuilder result = new StringBuilder();

        if (!file.exists()) {
            return StringUtils.EMPTY;
        }
        long count = 0;

        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "r");
            long length = randomAccessFile.length();
            if (length == 0L) {
                return StringUtils.EMPTY;
            } else {
                long pos = length - 1;
                while (pos > 0) {
                    pos--;
                    randomAccessFile.seek(pos);
                    if (randomAccessFile.readByte() == '\n') {
                        String line = randomAccessFile.readLine();
                        linesArray.add(new String(line.getBytes(StandardCharsets.ISO_8859_1),
                            StandardCharsets.UTF_8));
                        count++;
                        if (count == lines) {
                            break;
                        }
                    }
                }
                if (pos == 0) {
                    randomAccessFile.seek(0);
                    linesArray.add(new String(
                        randomAccessFile.readLine().getBytes(StandardCharsets.ISO_8859_1),
                        StandardCharsets.UTF_8));
                }
            }
        } catch (Exception e) {
            throw new ServiceException("读取日志失败", e);
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Collections.reverse(linesArray);

        linesArray.forEach(line -> result.append(line).append(StringUtils.LF));

        return result.toString();
    }

    @Override
    public LoginPreCheckDTO getUserEnv(@NonNull String username) {
        Assert.notNull(username, "username must not be null");

        boolean useMFA = true;
        try {
            final User user = ValidationUtils.isEmail(username)
                ? userService.getByEmailOfNonNull(username) :
                userService.getByUsernameOfNonNull(username);
            useMFA = MFAType.useMFA(user.getMfaType());
        } catch (NotFoundException e) {
            log.error("Failed to find user by name: " + username, e);
            eventPublisher
                .publishEvent(new LogEvent(this, username, LogType.LOGIN_FAILED, username));
        }
        return new LoginPreCheckDTO(useMFA);
    }
}
