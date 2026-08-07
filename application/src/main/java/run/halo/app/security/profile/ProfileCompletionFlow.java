package run.halo.app.security.profile;

import java.net.URI;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Coordinates ordered profile-completion requirements and continuation redirects. */
@Component
public class ProfileCompletionFlow {

    private static final URI DEFAULT_REDIRECT_URI = URI.create("/uc");

    private final ServerRequestCache requestCache;

    private final List<ProfileCompletionRequirement> requirements;

    public ProfileCompletionFlow(
            ServerRequestCache requestCache, ObjectProvider<ProfileCompletionRequirement> requirements) {
        this.requestCache = requestCache;
        this.requirements = requirements.orderedStream().toList();
    }

    /** Returns the first required profile-completion step for the user. */
    public Mono<ProfileCompletionStep> findNext(String username) {
        return Flux.fromIterable(requirements)
                .concatMap(requirement -> requirement.evaluate(username))
                .next();
    }

    /** Returns the next completion page, the saved request, or the user-center fallback. */
    public Mono<URI> getRedirectUri(String username, ServerWebExchange exchange) {
        return findNext(username)
                .map(ProfileCompletionStep::location)
                .switchIfEmpty(Mono.defer(() -> getRedirectUriAfterCompletion(exchange)));
    }

    /** Returns the saved request or the user-center fallback after all requirements are complete. */
    public Mono<URI> getRedirectUriAfterCompletion(ServerWebExchange exchange) {
        return requestCache.getRedirectUri(exchange).defaultIfEmpty(DEFAULT_REDIRECT_URI);
    }
}
