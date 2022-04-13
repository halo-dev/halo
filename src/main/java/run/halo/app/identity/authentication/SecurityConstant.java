package run.halo.app.identity.authentication;

/**
 * @author guqing
 * @date 2022-04-13
 */
public interface SecurityConstant {

    /**
     * 30 mins
     */
    long EXPIRATION_TIME = 1_800_000;

    /**
     * Token prefix
     */
    String TOKEN_PREFIX = "Bearer";

    /**
     * Authentication header
     */
    String HEADER_STRING = "Authorization";

    /**
     * login uri
     */
    String LOGIN_URL = "/api/v1/oauth/login";
}
