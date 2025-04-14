package run.halo.app.security.authentication.pat;

import static run.halo.app.extension.Comparators.compareCreationTimestamp;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Predicate;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.user.service.PatService;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.exception.AccessDeniedException;
import run.halo.app.security.PersonalAccessToken;

@Service
class UserScopedPatHandlerImpl implements UserScopedPatHandler {

    private static final String ACCESS_TOKEN_ANNO_NAME = "security.halo.run/access-token";

    private final ReactiveExtensionClient client;

    private final PatService patService;

    private final AuthenticationTrustResolver authTrustResolver =
        new AuthenticationTrustResolverImpl();

    public UserScopedPatHandlerImpl(ReactiveExtensionClient client,
        PatService patService) {
        this.client = client;
        this.patService = patService;
    }

    private Mono<Authentication> mustBeAuthenticated(Mono<Authentication> authentication) {
        return authentication.filter(authTrustResolver::isAuthenticated)
            // Non-username-password authentication could not access the API at any time.
            .switchIfEmpty(Mono.error(AccessDeniedException::new));
    }

    @Override
    public Mono<ServerResponse> create(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .transform(this::mustBeAuthenticated)
            .flatMap(auth -> request.bodyToMono(PersonalAccessToken.class))
            .switchIfEmpty(Mono.error(() -> new ServerWebInputException("Missing request body.")))
            .flatMap(patService::create)
            .flatMap(pat -> patService.generateToken(pat)
                .doOnNext(token -> {
                    if (pat.getMetadata().getAnnotations() == null) {
                        pat.getMetadata().setAnnotations(new HashMap<>());
                    }
                    pat.getMetadata().getAnnotations()
                        .put(ACCESS_TOKEN_ANNO_NAME, token);
                })
                .thenReturn(pat)
            )
            .flatMap(pat -> ServerResponse.ok().bodyValue(pat));
    }

    @Override
    public Mono<ServerResponse> list(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .flatMap(auth -> {
                Predicate<PersonalAccessToken> predicate =
                    pat -> Objects.equals(auth.getName(), pat.getSpec().getUsername());
                var pats = client.list(PersonalAccessToken.class, predicate,
                    compareCreationTimestamp(false));
                return ServerResponse.ok().body(pats, PersonalAccessToken.class);
            });
    }

    @Override
    public Mono<ServerResponse> get(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .flatMap(auth -> {
                var name = request.pathVariable("name");
                return patService.get(name, auth.getName());
            })
            .flatMap(pat -> ServerResponse.ok().bodyValue(pat));
    }

    @Override
    public Mono<ServerResponse> revoke(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .flatMap(auth -> {
                var name = request.pathVariable("name");
                return patService.revoke(name, auth.getName());
            })
            .flatMap(revokedPat -> ServerResponse.ok().bodyValue(revokedPat));
    }

    @Override
    public Mono<ServerResponse> delete(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .flatMap(auth -> {
                var name = request.pathVariable("name");
                return patService.delete(name, auth.getName());
            })
            .flatMap(pat -> ServerResponse.ok().bodyValue(pat));
    }

    @Override
    public Mono<ServerResponse> restore(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .transform(this::mustBeAuthenticated)
            .flatMap(auth -> {
                var name = request.pathVariable("name");
                return patService.restore(name, auth.getName());
            })
            .flatMap(pat -> ServerResponse.ok().bodyValue(pat));
    }

}
