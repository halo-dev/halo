package run.halo.app.service;

import org.springframework.lang.NonNull;
import run.halo.app.model.dto.StatisticDTO;
import run.halo.app.model.params.LoginParam;
import run.halo.app.security.token.AuthToken;

/**
 * Admin service.
 *
 * @author johnniang
 * @date 19-4-29
 */
public interface AdminService {

    String ACCESS_TOKEN_CACHE_PREFIX = "halo.admin.access_token.";

    String REFRESH_TOKEN_CACHE_PREFIX = "halo.admin.refresh_token.";

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
     * Get system counts.
     *
     * @return count dto
     */
    @NonNull
    StatisticDTO getCount();
}
