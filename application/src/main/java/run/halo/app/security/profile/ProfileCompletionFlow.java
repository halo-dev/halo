package run.halo.app.security.profile;

import java.net.URI;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.core.user.service.UserService;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;

/** Coordinates ordered profile-completion requirements and continuation redirects. */
@Component
public class ProfileCompletionFlow {

    private static final URI DEFAULT_REDIRECT_URI = URI.create("/uc");

    private final UserService userService;

    private final SystemConfigFetcher systemConfigFetcher;

    private final ServerRequestCache requestCache;

    private final List<ProfileCompletionRequirement> requirements;

    public ProfileCompletionFlow(
            UserService userService,
            SystemConfigFetcher systemConfigFetcher,
            ServerRequestCache requestCache,
            ObjectProvider<ProfileCompletionRequirement> requirements) {
        this.userService = userService;
        this.systemConfigFetcher = systemConfigFetcher;
        this.requestCache = requestCache;
        this.requirements = requirements.orderedStream().toList();
    }

    /** Returns the first required profile-completion step for the user. */
    public Mono<ProfileCompletionStep> findNext(String username) {
        return Mono.zip(
                        userService.getUser(username),
                        systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .flatMap(tuple -> requirements.stream()
                        .map(requirement -> requirement.evaluate(tuple.getT1(), tuple.getT2()))
                        .flatMap(java.util.Optional::stream)
                        .findFirst()
                        .map(Mono::just)
                        .orElseGet(Mono::empty));
    }

    /** Returns the next completion page, the saved request, or the user-center fallback. */
    public Mono<URI> getRedirectUri(String username, ServerWebExchange exchange) {
        return findNext(username)
                .map(ProfileCompletionStep::location)
                .switchIfEmpty(Mono.defer(() -> requestCache.getRedirectUri(exchange)))
                .defaultIfEmpty(DEFAULT_REDIRECT_URI);
    }
}
