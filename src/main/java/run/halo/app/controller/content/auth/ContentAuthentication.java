package run.halo.app.controller.content.auth;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import run.halo.app.utils.ServletUtils;

/**
 * Content authentication.
 *
 * @author guqing
 * @date 2022-02-23
 */
public interface ContentAuthentication {

    /**
     * The identity of the principal being authenticated.
     *
     * @return authentication principal.
     */
    Object getPrincipal();

    /**
     * whether the resource been authenticated by a sessionId.
     *
     * @param resourceId resourceId to authentication
     * @see HttpServletRequest#getRequestedSessionId()
     * @return true if the resourceId has been authenticated by a sessionId
     */
    boolean isAuthenticated(Integer resourceId);

    /**
     * Set authentication state.
     *
     * @param resourceId resource identity
     * @param isAuthenticated authentication state
     * @see HttpServletRequest#getRequestedSessionId()
     */
    void setAuthenticated(Integer resourceId, boolean isAuthenticated);

    /**
     * Clear authentication state.
     *
     * @param resourceId resource id.
     */
    void clearByResourceId(Integer resourceId);

    String CACHE_PREFIX = "CONTENT_AUTHENTICATED";

    /**
     * build authentication cache key.
     *
     * @param sessionId session id
     * @param principal authentication principal
     * @param value principal identity
     * @return cache key
     */
    default String buildCacheKey(String sessionId, String principal,
        String value) {
        Assert.notNull(sessionId, "The sessionId must not be null.");
        Assert.notNull(principal, "The principal must not be null.");
        Assert.notNull(value, "The value must not be null.");
        return StringUtils.joinWith(":", CACHE_PREFIX, principal, value, sessionId);
    }

    /**
     * Gets request session id.
     *
     * @return request session id.
     */
    default String getSessionId() {
        Optional<HttpServletRequest> currentRequest = ServletUtils.getCurrentRequest();
        if (currentRequest.isEmpty()) {
            return StringUtils.EMPTY;
        }
        return currentRequest.get().getRequestedSessionId();
    }
}
