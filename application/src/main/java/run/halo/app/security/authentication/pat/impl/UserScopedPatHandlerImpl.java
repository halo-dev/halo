package run.halo.app.security.authentication.pat.impl;

import static run.halo.app.extension.Comparators.compareCreationTimestamp;
import static run.halo.app.security.authentication.pat.PatServerWebExchangeMatcher.PAT_TOKEN_PREFIX;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.time.Clock;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.CollectionUtils;
import org.springframework.util.IdGenerator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.exception.ExtensionNotFoundException;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.exception.AccessDeniedException;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.security.PersonalAccessToken;
import run.halo.app.security.authentication.CryptoService;
import run.halo.app.security.authentication.pat.UserScopedPatHandler;
import run.halo.app.security.authorization.AuthorityUtils;

@Service
public class UserScopedPatHandlerImpl implements UserScopedPatHandler {

    private static final String ACCESS_TOKEN_ANNO_NAME = "security.halo.run/access-token";

    private static final NotFoundException PAT_NOT_FOUND_EX =
        new NotFoundException("The personal access token was not found or deleted.");

    private final ReactiveExtensionClient client;

    private final JwtEncoder patEncoder;

    private final ExternalUrlSupplier externalUrl;

    private final RoleService roleService;

    private final IdGenerator idGenerator;

    private final String keyId;

    private Clock clock;

    public UserScopedPatHandlerImpl(ReactiveExtensionClient client,
        CryptoService cryptoService,
        ExternalUrlSupplier externalUrl,
        RoleService roleService) {
        this.client = client;
        this.externalUrl = externalUrl;
        this.roleService = roleService;

        var patJwk = cryptoService.getJwk();
        var jwkSet = new ImmutableJWKSet<>(new JWKSet(patJwk));
        this.patEncoder = new NimbusJwtEncoder(jwkSet);
        this.keyId = patJwk.getKeyID();
        this.idGenerator = new AlternativeJdkIdGenerator();
        this.clock = Clock.systemDefaultZone();
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    private static Mono<Authentication> mustBeRealUser(Mono<Authentication> authentication) {
        return authentication.filter(AuthorityUtils::isRealUser)
            // Non-username-password authentication could not access the API at any time.
            .switchIfEmpty(Mono.error(AccessDeniedException::new));
    }

    @Override
    public Mono<ServerResponse> create(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .transform(UserScopedPatHandlerImpl::mustBeRealUser)
            .flatMap(auth -> request.bodyToMono(PersonalAccessToken.class)
                .switchIfEmpty(
                    Mono.error(() -> new ServerWebInputException("Missing request body.")))
                .flatMap(patRequest -> {
                    var patSpec = patRequest.getSpec();
                    var roles = patSpec.getRoles();
                    var rolesCheck = hasSufficientRoles(auth.getAuthorities(), roles)
                        .filter(has -> has)
                        .switchIfEmpty(
                            Mono.error(() -> new ServerWebInputException("Insufficient roles.")))
                        .then();

                    var expiresCheck = Mono.fromRunnable(() -> {
                        var expiresAt = patSpec.getExpiresAt();
                        var now = clock.instant();
                        if (expiresAt != null && (now.isAfter(expiresAt))) {
                            throw new ServerWebInputException("Invalid expiresAt.");
                        }
                    }).then();

                    var createPat = Mono.defer(() -> {
                        var pat = new PersonalAccessToken();
                        var spec = pat.getSpec();
                        spec.setUsername(auth.getName());
                        spec.setName(patSpec.getName());
                        spec.setDescription(patSpec.getDescription());
                        spec.setRoles(patSpec.getRoles());
                        spec.setScopes(patSpec.getScopes());
                        spec.setExpiresAt(patSpec.getExpiresAt());
                        var tokenId = idGenerator.generateId().toString();
                        spec.setTokenId(tokenId);
                        var metadata = new Metadata();
                        metadata.setGenerateName("pat-" + auth.getName() + "-");
                        pat.setMetadata(metadata);
                        return client.create(pat)
                            .doOnNext(createdPat -> {
                                var claimsBuilder = JwtClaimsSet.builder()
                                    .issuer(externalUrl.getURL(request.exchange().getRequest())
                                        .toString())
                                    .id(tokenId)
                                    .subject(auth.getName())
                                    .issuedAt(clock.instant())
                                    .claim("pat_name", createdPat.getMetadata().getName());
                                var expiresAt = createdPat.getSpec().getExpiresAt();
                                if (expiresAt != null) {
                                    claimsBuilder.expiresAt(expiresAt);
                                }
                                var headerBuilder = JwsHeader.with(SignatureAlgorithm.RS256)
                                    .keyId(this.keyId);
                                var jwt = patEncoder.encode(JwtEncoderParameters.from(
                                    headerBuilder.build(),
                                    claimsBuilder.build()));
                                var annotations =
                                    createdPat.getMetadata().getAnnotations();
                                if (annotations == null) {
                                    annotations = new HashMap<>();
                                    createdPat.getMetadata().setAnnotations(annotations);
                                }
                                annotations.put(ACCESS_TOKEN_ANNO_NAME,
                                    PAT_TOKEN_PREFIX + jwt.getTokenValue());
                            });
                    });
                    return rolesCheck.and(expiresCheck).then(createPat)
                        .flatMap(createdPat -> ServerResponse.ok().bodyValue(createdPat));
                }));
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
                var pat = getPat(name, auth.getName());
                return ServerResponse.ok().body(pat, PersonalAccessToken.class);
            });
    }

    @Override
    public Mono<ServerResponse> revoke(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .flatMap(auth -> {
                var name = request.pathVariable("name");
                var revokedPat = getPat(name, auth.getName())
                    .filter(pat -> !pat.getSpec().isRevoked())
                    .switchIfEmpty(Mono.error(
                        () -> new ServerWebInputException("The token has been revoked before.")))
                    .doOnNext(pat -> {
                        var spec = pat.getSpec();
                        spec.setRevoked(true);
                        spec.setRevokesAt(clock.instant());
                    })
                    .flatMap(client::update);
                return ServerResponse.ok().body(revokedPat, PersonalAccessToken.class);
            });
    }

    @Override
    public Mono<ServerResponse> delete(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .flatMap(auth -> {
                var name = request.pathVariable("name");
                var deletedPat = getPat(name, auth.getName())
                    .flatMap(client::delete);
                return ServerResponse.ok().body(deletedPat, PersonalAccessToken.class);
            });
    }

    @Override
    public Mono<ServerResponse> restore(ServerRequest request) {
        var restoredPat = ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .transform(UserScopedPatHandlerImpl::mustBeRealUser)
            .flatMap(auth -> {
                var name = request.pathVariable("name");
                return getPat(name, auth.getName());
            })
            .filter(pat -> pat.getSpec().isRevoked())
            .switchIfEmpty(Mono.error(
                () -> new ServerWebInputException(
                    "The token has not been revoked before.")))
            .doOnNext(pat -> {
                var spec = pat.getSpec();
                spec.setRevoked(false);
                spec.setRevokesAt(null);
            })
            .flatMap(client::update);
        return ServerResponse.ok().body(restoredPat, PersonalAccessToken.class);
    }

    private Mono<Boolean> hasSufficientRoles(
        Collection<? extends GrantedAuthority> grantedAuthorities, List<String> requestRoles) {
        if (CollectionUtils.isEmpty(requestRoles)) {
            return Mono.just(true);
        }
        var grantedRoles = AuthorityUtils.authoritiesToRoles(grantedAuthorities);
        return roleService.contains(grantedRoles, requestRoles);
    }

    private Mono<PersonalAccessToken> getPat(String name, String username) {
        return client.get(PersonalAccessToken.class, name)
            .filter(pat -> Objects.equals(pat.getSpec().getUsername(), username)
                && !ExtensionUtil.isDeleted(pat))
            .onErrorMap(ExtensionNotFoundException.class, t -> PAT_NOT_FOUND_EX)
            .switchIfEmpty(Mono.error(() -> PAT_NOT_FOUND_EX));
    }
}
