package run.halo.app.service.impl;

import cn.hutool.core.lang.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.exception.BadRequestException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.dto.EnvironmentDTO;
import run.halo.app.model.dto.StatisticDTO;
import run.halo.app.model.entity.User;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.params.LoginParam;
import run.halo.app.security.authentication.Authentication;
import run.halo.app.security.context.SecurityContextHolder;
import run.halo.app.security.token.AuthToken;
import run.halo.app.security.util.SecurityUtils;
import run.halo.app.service.*;
import run.halo.app.utils.HaloUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.TimeUnit;

/**
 * Admin service implementation.
 *
 * @author johnniang
 * @author ryanwang
 * @date 19-4-29
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

    private final StringCacheStore cacheStore;

    private final String driverClassName;

    public AdminServiceImpl(PostService postService,
                            SheetService sheetService,
                            AttachmentService attachmentService,
                            PostCommentService postCommentService,
                            SheetCommentService sheetCommentService,
                            JournalCommentService journalCommentService,
                            OptionService optionService,
                            UserService userService,
                            LinkService linkService,
                            StringCacheStore cacheStore,
                            @Value("${spring.datasource.driver-class-name}") String driverClassName) {
        this.postService = postService;
        this.sheetService = sheetService;
        this.attachmentService = attachmentService;
        this.postCommentService = postCommentService;
        this.sheetCommentService = sheetCommentService;
        this.journalCommentService = journalCommentService;
        this.optionService = optionService;
        this.userService = userService;
        this.linkService = linkService;
        this.cacheStore = cacheStore;
        this.driverClassName = driverClassName;
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
            throw new BadRequestException(mismatchTip);
        }

        userService.mustNotExpire(user);

        if (!userService.passwordMatch(user, loginParam.getPassword())) {
            // If the password is mismatch
            throw new BadRequestException(mismatchTip);
        }

        if (SecurityContextHolder.getContext().isAuthenticated()) {
            // If the user has been logged in
            throw new BadRequestException("You have been logged in, do not log in repeatedly please");
        }

        // Generate new token
        return buildAuthToken(user);
    }

    @Override
    public void clearToken() {
        // Check if the current is logging in
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new BadRequestException("You haven't logged in yet, so you can't log out");
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

        log.info("You have been logged out, looking forward to your next visit!");
    }

    @Override
    public StatisticDTO getCount() {
        StatisticDTO statisticDTO = new StatisticDTO();
        statisticDTO.setPostCount(postService.countByStatus(PostStatus.PUBLISHED));
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
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        environmentDTO.setStartTime(runtimeMXBean.getStartTime());

        environmentDTO.setDatabase("org.h2.Driver".equals(driverClassName) ? "H2" : "MySQL");

        return environmentDTO;
    }

    @Override
    public AuthToken refreshToken(String refreshToken) {
        Assert.hasText(refreshToken, "Refresh token must not be blank");

        Integer userId = cacheStore.getAny(SecurityUtils.buildTokenRefreshKey(refreshToken), Integer.class)
                .orElseThrow(() -> new BadRequestException("The refresh token may have been expired already").setErrorData(refreshToken));

        // Get user info
        User user = userService.getById(userId);

        // Remove all token
        cacheStore.getAny(SecurityUtils.buildAccessTokenKey(user), String.class).ifPresent(accessToken -> {
            cacheStore.delete(SecurityUtils.buildTokenAccessKey(accessToken));
        });
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
        cacheStore.putAny(SecurityUtils.buildAccessTokenKey(user), token.getAccessToken(), REFRESH_TOKEN_EXPIRED_DAYS, TimeUnit.DAYS);
        cacheStore.putAny(SecurityUtils.buildRefreshTokenKey(user), token.getRefreshToken(), REFRESH_TOKEN_EXPIRED_DAYS, TimeUnit.DAYS);

        // Cache those tokens with user id
        cacheStore.putAny(SecurityUtils.buildTokenAccessKey(token.getAccessToken()), user.getId(), ACCESS_TOKEN_EXPIRED_SECONDS, TimeUnit.SECONDS);
        cacheStore.putAny(SecurityUtils.buildTokenRefreshKey(token.getRefreshToken()), user.getId(), REFRESH_TOKEN_EXPIRED_DAYS, TimeUnit.DAYS);

        return token;
    }
}
