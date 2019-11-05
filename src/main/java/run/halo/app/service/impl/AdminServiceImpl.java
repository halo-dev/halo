package run.halo.app.service.impl;

import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.event.logger.LogEvent;
import run.halo.app.exception.BadRequestException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.exception.ServiceException;
import run.halo.app.model.dto.EnvironmentDTO;
import run.halo.app.model.dto.StatisticDTO;
import run.halo.app.model.entity.User;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.model.enums.LogType;
import run.halo.app.model.enums.Mode;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.params.LoginParam;
import run.halo.app.model.params.ResetPasswordParam;
import run.halo.app.model.properties.EmailProperties;
import run.halo.app.model.support.HaloConst;
import run.halo.app.security.authentication.Authentication;
import run.halo.app.security.context.SecurityContextHolder;
import run.halo.app.security.token.AuthToken;
import run.halo.app.security.util.SecurityUtils;
import run.halo.app.service.*;
import run.halo.app.utils.FileUtils;
import run.halo.app.utils.HaloUtils;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static run.halo.app.model.support.HaloConst.*;

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

    private final PostService postService;

    private final SheetService sheetService;

    private final AttachmentService attachmentService;

    private final PostCommentService postCommentService;

    private final SheetCommentService sheetCommentService;

    private final JournalCommentService journalCommentService;

    private final OptionService optionService;

    private final UserService userService;

    private final LinkService linkService;

    private final MailService mailService;

    private final StringCacheStore cacheStore;

    private final RestTemplate restTemplate;

    private final HaloProperties haloProperties;

    private final ApplicationEventPublisher eventPublisher;

    private final String driverClassName;

    private final String mode;

    public AdminServiceImpl(PostService postService,
                            SheetService sheetService,
                            AttachmentService attachmentService,
                            PostCommentService postCommentService,
                            SheetCommentService sheetCommentService,
                            JournalCommentService journalCommentService,
                            OptionService optionService,
                            UserService userService,
                            LinkService linkService,
                            MailService mailService,
                            StringCacheStore cacheStore,
                            RestTemplate restTemplate,
                            HaloProperties haloProperties,
                            ApplicationEventPublisher eventPublisher,
                            @Value("${spring.datasource.driver-class-name}") String driverClassName,
                            @Value("${spring.profiles.active:prod}") String mode) {
        this.postService = postService;
        this.sheetService = sheetService;
        this.attachmentService = attachmentService;
        this.postCommentService = postCommentService;
        this.sheetCommentService = sheetCommentService;
        this.journalCommentService = journalCommentService;
        this.optionService = optionService;
        this.userService = userService;
        this.linkService = linkService;
        this.mailService = mailService;
        this.cacheStore = cacheStore;
        this.restTemplate = restTemplate;
        this.haloProperties = haloProperties;
        this.eventPublisher = eventPublisher;
        this.driverClassName = driverClassName;
        this.mode = mode;
    }

    @Override
    public AuthToken authenticate(LoginParam loginParam) {
        Assert.notNull(loginParam, "Login param must not be null");

        String username = loginParam.getUsername();

        String mismatchTip = "用户名或者密码不正确";

        final User user;

        try {
            // Get user by username or email
            user = Validator.isEmail(username) ?
                    userService.getByEmailOfNonNull(username) : userService.getByUsernameOfNonNull(username);
        } catch (NotFoundException e) {
            log.error("Failed to find user by name: " + username, e);
            eventPublisher.publishEvent(new LogEvent(this, loginParam.getUsername(), LogType.LOGIN_FAILED, loginParam.getUsername()));

            throw new BadRequestException(mismatchTip);
        }

        userService.mustNotExpire(user);

        if (!userService.passwordMatch(user, loginParam.getPassword())) {
            // If the password is mismatch
            eventPublisher.publishEvent(new LogEvent(this, loginParam.getUsername(), LogType.LOGIN_FAILED, loginParam.getUsername()));

            throw new BadRequestException(mismatchTip);
        }

        if (SecurityContextHolder.getContext().isAuthenticated()) {
            // If the user has been logged in
            throw new BadRequestException("您已登录，请不要重复登录");
        }

        // Log it then login successful
        eventPublisher.publishEvent(new LogEvent(this, user.getUsername(), LogType.LOGGED_IN, user.getNickname()));

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
        cacheStore.getAny(SecurityUtils.buildAccessTokenKey(user), String.class).ifPresent(accessToken -> {
            // Delete token
            cacheStore.delete(SecurityUtils.buildTokenAccessKey(accessToken));
            cacheStore.delete(SecurityUtils.buildAccessTokenKey(user));
        });

        // Clear refresh token
        cacheStore.getAny(SecurityUtils.buildRefreshTokenKey(user), String.class).ifPresent(refreshToken -> {
            cacheStore.delete(SecurityUtils.buildTokenRefreshKey(refreshToken));
            cacheStore.delete(SecurityUtils.buildRefreshTokenKey(user));
        });

        eventPublisher.publishEvent(new LogEvent(this, user.getUsername(), LogType.LOGGED_OUT, user.getNickname()));

        log.info("You have been logged out, looking forward to your next visit!");
    }

    @Override
    public void sendResetPasswordCode(ResetPasswordParam param) {
        cacheStore.getAny("code", String.class).ifPresent(code -> {
            throw new ServiceException("已经获取过验证码，不能重复获取");
        });

        Boolean emailEnabled = optionService.getByPropertyOrDefault(EmailProperties.ENABLED, Boolean.class, false);

        if (!emailEnabled) {
            throw new ServiceException("未启用 SMTP 服务");
        }

        if (!userService.verifyUser(param.getUsername(), param.getEmail())) {
            throw new ServiceException("用户名或者邮箱验证错误");
        }

        // Gets random code.
        String code = RandomUtil.randomNumbers(6);

        log.info("Get reset password code:{}", code);

        // Send email to administrator.
        String content = "您正在进行密码重置操作，如不是本人操作，请尽快做好相应措施。密码重置验证码如下（五分钟有效）：\n" + code;
        mailService.sendMail(param.getEmail(), "找回密码验证码", content);

        // Cache code.
        cacheStore.putAny("code", code, 5, TimeUnit.MINUTES);
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
        String code = cacheStore.getAny("code", String.class).orElseThrow(() -> new ServiceException("未获取过验证码"));
        if (!code.equals(param.getCode())) {
            throw new ServiceException("验证码不正确");
        }

        User user = userService.getCurrentUser().orElseThrow(() -> new ServiceException("未查询到博主信息"));

        // reset password
        userService.setPassword(user, param.getPassword());

        // Update this user
        userService.update(user);

        // clear code cache
        cacheStore.delete("code");
    }

    @Override
    public StatisticDTO getCount() {
        StatisticDTO statisticDTO = new StatisticDTO();
        statisticDTO.setPostCount(postService.countByStatus(PostStatus.PUBLISHED) + sheetService.countByStatus(PostStatus.PUBLISHED));
        statisticDTO.setAttachmentCount(attachmentService.count());

        // Handle comment count
        long postCommentCount = postCommentService.countByStatus(CommentStatus.PUBLISHED);
        long sheetCommentCount = sheetCommentService.countByStatus(CommentStatus.PUBLISHED);
        long journalCommentCount = journalCommentService.countByStatus(CommentStatus.PUBLISHED);

        statisticDTO.setCommentCount(postCommentCount + sheetCommentCount + journalCommentCount);

        long birthday = optionService.getBirthday();
        long days = (System.currentTimeMillis() - birthday) / (1000 * 24 * 3600);
        statisticDTO.setEstablishDays(days);
        statisticDTO.setBirthday(birthday);

        statisticDTO.setLinkCount(linkService.count());

        statisticDTO.setVisitCount(postService.countVisit() + sheetService.countVisit());
        statisticDTO.setLikeCount(postService.countLike() + sheetService.countLike());
        return statisticDTO;
    }

    @Override
    public EnvironmentDTO getEnvironments() {
        EnvironmentDTO environmentDTO = new EnvironmentDTO();

        // Get application start time.
        environmentDTO.setStartTime(ManagementFactory.getRuntimeMXBean().getStartTime());

        environmentDTO.setDatabase("org.h2.Driver".equals(driverClassName) ? "H2" : "MySQL");

        environmentDTO.setVersion(HaloConst.HALO_VERSION);

        environmentDTO.setMode(Mode.valueFrom(this.mode));

        return environmentDTO;
    }

    @Override
    public AuthToken refreshToken(String refreshToken) {
        Assert.hasText(refreshToken, "Refresh token must not be blank");

        Integer userId = cacheStore.getAny(SecurityUtils.buildTokenRefreshKey(refreshToken), Integer.class)
                .orElseThrow(() -> new BadRequestException("登陆状态已失效，请重新登陆").setErrorData(refreshToken));

        // Get user info
        User user = userService.getById(userId);

        // Remove all token
        cacheStore.getAny(SecurityUtils.buildAccessTokenKey(user), String.class)
                .ifPresent(accessToken -> cacheStore.delete(SecurityUtils.buildTokenAccessKey(accessToken)));
        cacheStore.delete(SecurityUtils.buildTokenRefreshKey(refreshToken));
        cacheStore.delete(SecurityUtils.buildAccessTokenKey(user));
        cacheStore.delete(SecurityUtils.buildRefreshTokenKey(user));

        return buildAuthToken(user);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateAdminAssets() {
        // Request github api
        ResponseEntity<Map> responseEntity = restTemplate.getForEntity(HaloConst.HALO_ADMIN_RELEASES_LATEST, Map.class);

        if (responseEntity == null ||
                responseEntity.getStatusCode().isError() ||
                responseEntity.getBody() == null) {
            log.debug("Failed to request remote url: [{}]", HALO_ADMIN_RELEASES_LATEST);
            throw new ServiceException("系统无法访问到 Github 的 API").setErrorData(HALO_ADMIN_RELEASES_LATEST);
        }

        Object assetsObject = responseEntity.getBody().get("assets");

        if (assetsObject instanceof List) {
            try {
                List assets = (List) assetsObject;
                Map assetMap = (Map) assets.stream()
                        .filter(assetPredicate())
                        .findFirst()
                        .orElseThrow(() -> new ServiceException("Halo admin 最新版暂无资源文件，请稍后再试"));

                Object browserDownloadUrl = assetMap.getOrDefault("browser_download_url", "");
                // Download the assets
                ResponseEntity<byte[]> downloadResponseEntity = restTemplate.getForEntity(browserDownloadUrl.toString(), byte[].class);

                if (downloadResponseEntity == null ||
                        downloadResponseEntity.getStatusCode().isError() ||
                        downloadResponseEntity.getBody() == null) {
                    throw new ServiceException("Failed to request remote url: " + browserDownloadUrl.toString()).setErrorData(browserDownloadUrl.toString());
                }

                String adminTargetName = haloProperties.getWorkDir() + HALO_ADMIN_RELATIVE_PATH;

                Path adminPath = Paths.get(adminTargetName);
                Path adminBackupPath = Paths.get(haloProperties.getWorkDir(), HALO_ADMIN_RELATIVE_BACKUP_PATH);

                backupAndClearAdminAssetsIfPresent(adminPath, adminBackupPath);

                // Create temp folder
                Path assetTempPath = FileUtils.createTempDirectory()
                        .resolve(assetMap.getOrDefault("name", "halo-admin-latest.zip").toString());

                // Unzip
                FileUtils.unzip(downloadResponseEntity.getBody(), assetTempPath);

                // Copy it to template/admin folder
                FileUtils.copyFolder(FileUtils.tryToSkipZipParentFolder(assetTempPath), adminPath);
            } catch (Throwable t) {
                log.error("Failed to update halo admin", t);
                throw new ServiceException("更新 Halo admin 失败");
            }
        } else {
            throw new ServiceException("Github API 返回内容有误").setErrorData(assetsObject);
        }
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private Predicate<Object> assetPredicate() {
        return asset -> {
            if (!(asset instanceof Map)) {
                return false;
            }
            Map aAssetMap = (Map) asset;
            // Get content-type
            String contentType = aAssetMap.getOrDefault("content_type", "").toString();

            Object name = aAssetMap.getOrDefault("name", "");
            return name.toString().matches(HALO_ADMIN_VERSION_REGEX) && "application/zip".equalsIgnoreCase(contentType);
        };
    }

    private void backupAndClearAdminAssetsIfPresent(@NonNull Path sourcePath, @NonNull Path backupPath) throws IOException {
        Assert.notNull(sourcePath, "Source path must not be null");
        Assert.notNull(backupPath, "Backup path must not be null");

        if (!FileUtils.isEmpty(sourcePath)) {
            // Clone this assets
            Path adminPathBackup = Paths.get(haloProperties.getWorkDir(), HALO_ADMIN_RELATIVE_BACKUP_PATH);

            // Delete backup
            FileUtils.deleteFolder(backupPath);

            // Copy older assets into backup
            FileUtils.copyFolder(sourcePath, backupPath);

            // Delete older assets
            FileUtils.deleteFolder(sourcePath);
        } else {
            FileUtils.createIfAbsent(sourcePath);
        }
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
        cacheStore.putAny(SecurityUtils.buildAccessTokenKey(user), token.getAccessToken(), REFRESH_TOKEN_EXPIRED_DAYS, TimeUnit.DAYS);
        cacheStore.putAny(SecurityUtils.buildRefreshTokenKey(user), token.getRefreshToken(), REFRESH_TOKEN_EXPIRED_DAYS, TimeUnit.DAYS);

        // Cache those tokens with user id
        cacheStore.putAny(SecurityUtils.buildTokenAccessKey(token.getAccessToken()), user.getId(), ACCESS_TOKEN_EXPIRED_SECONDS, TimeUnit.SECONDS);
        cacheStore.putAny(SecurityUtils.buildTokenRefreshKey(token.getRefreshToken()), user.getId(), REFRESH_TOKEN_EXPIRED_DAYS, TimeUnit.DAYS);

        return token;
    }

    @Override
    public String getSpringLogs() {
        File file = new File(haloProperties.getWorkDir(), LOGS_PATH);
        if (!file.exists()) {
            return "暂无日志";
        }
        FileReader reader = new FileReader(file);
        return reader.readString();
    }
}
