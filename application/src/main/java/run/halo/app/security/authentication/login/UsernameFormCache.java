package run.halo.app.security.authentication.login;

import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * A cache for storing the last login username in the WebSession, so it can be pre-filled on the
 * next visit to the login page.
 *
 * @author johnniang
 */
public class UsernameFormCache {

    static final String SESSION_KEY = UsernameFormCache.class + ".USERNAME";

    /**
     * Saves the username to the WebSession. If the username is blank, nothing is saved.
     *
     * @param exchange the current server web exchange
     * @param username the username to save
     * @return a Mono that completes when the username has been saved
     */
    public static Mono<Void> save(ServerWebExchange exchange, String username) {
        if (!StringUtils.hasText(username)) {
            return Mono.empty();
        }
        return exchange.getSession()
            .doOnNext(session -> session.getAttributes().put(SESSION_KEY, username))
            .then();
    }

    /**
     * Loads the username from the WebSession.
     *
     * @param exchange the current server web exchange
     * @return a Mono that emits the username, or empty if not present
     */
    public static Mono<String> load(ServerWebExchange exchange) {
        return exchange.getSession()
            .mapNotNull(session -> (String) session.getAttribute(SESSION_KEY));
    }

}
