package run.halo.app.service;

import org.springframework.lang.NonNull;
import run.halo.app.model.dto.EnvironmentDTO;
import run.halo.app.model.dto.StatisticDTO;
import run.halo.app.model.params.LoginParam;
import run.halo.app.model.params.ResetPasswordParam;
import run.halo.app.security.token.AuthToken;

/**
 * Admin service interface.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-04-29
 */
public interface AdminService {

    /**
     * Expired seconds.
     */
    int ACCESS_TOKEN_EXPIRED_SECONDS = 24 * 3600;

    int REFRESH_TOKEN_EXPIRED_DAYS = 30;

    String ACCESS_TOKEN_CACHE_PREFIX = "halo.admin.access_token.";

    String REFRESH_TOKEN_CACHE_PREFIX = "halo.admin.refresh_token.";

    String LOGS_PATH = "logs/spring.log";

    /**
     * Authenticates.
     *
     * @param loginParam login param must not be null
     * @return authentication token
     */
    @NonNull
    AuthToken authenticate(@NonNull LoginParam loginParam);

    /**
     * Clears authentication.
     */
    void clearToken();

    /**
     * Send reset password code to administrator's email.
     *
     * @param param param must not be null
     */
    void sendResetPasswordCode(@NonNull ResetPasswordParam param);

    /**
     * Reset password by code.
     *
     * @param param param must not be null
     */
    void resetPasswordByCode(@NonNull ResetPasswordParam param);

    /**
     * Get system counts.
     *
     * @return count dto
     */
    @NonNull
    StatisticDTO getCount();

    /**
     * Get system environments
     *
     * @return environments
     */
    @NonNull
    EnvironmentDTO getEnvironments();

    /**
     * Refreshes token.
     *
     * @param refreshToken refresh token must not be blank
     * @return authentication token
     */
    @NonNull
    AuthToken refreshToken(@NonNull String refreshToken);

    /**
     * Updates halo admin assets.
     */
    void updateAdminAssets();

    /**
     * Get spring logs.
     *
     * @return recently logs.
     */
    String getSpringLogs();
}
