package run.halo.app.core.user.service.impl;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
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
import org.springframework.web.filter.reactive.ServerWebExchangeContextFilter;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.user.service.PatService;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.security.PersonalAccessToken;
import run.halo.app.security.authentication.CryptoService;
import run.halo.app.security.authorization.AuthorityUtils;

/**
 * Service for managing personal access tokens (PATs).
 *
 * @author johnniang
 */
@Service
class PatServiceImpl implements PatService {

    private final RoleService roleService;

    private final IdGenerator idGenerator;

    private final ReactiveExtensionClient client;

    private final AuthenticationTrustResolver authTrustResolver =
        new AuthenticationTrustResolverImpl();

    private final JwtEncoder jwtEncoder;

    private final ExternalUrlSupplier externalUrl;

    private final ReactiveUserDetailsService userDetailsService;

    private final String keyId;

    private Clock clock;

    public PatServiceImpl(RoleService roleService,
        ReactiveExtensionClient client,
        ExternalUrlSupplier externalUrl,
        CryptoService cryptoService, ReactiveUserDetailsService userDetailsService) {
        this.roleService = roleService;
        this.client = client;
        this.externalUrl = externalUrl;
        this.userDetailsService = userDetailsService;
        this.clock = Clock.systemUTC();
        idGenerator = new AlternativeJdkIdGenerator();
        var jwk = cryptoService.getJwk();
        this.jwtEncoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
        this.keyId = jwk.getKeyID();
    }

    /**
     * Set clock for testing.
     *
     * @param clock the clock to set
     */
    void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Mono<PersonalAccessToken> create(PersonalAccessToken patRequest) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            // TODO We only allow authenticated users to create PATs.
            .filter(authTrustResolver::isAuthenticated)
            .switchIfEmpty(
                Mono.error(() -> new ServerWebInputException("Authentication required."))
            )
            .flatMap(auth ->
                create(patRequest, auth.getName(), auth.getAuthorities())
            );
    }

    @Override
    public Mono<PersonalAccessToken> create(PersonalAccessToken patRequest, String username) {
        return userDetailsService.findByUsername(username)
            .flatMap(userDetails ->
                create(patRequest, username, userDetails.getAuthorities())
            );
    }

    private Mono<PersonalAccessToken> create(PersonalAccessToken patRequest, String username,
        Collection<? extends GrantedAuthority> authorities) {
        var patSpec = patRequest.getSpec();
        // preflight check
        var expiresAt = patSpec.getExpiresAt();
        if (expiresAt != null && expiresAt.isBefore(clock.instant())) {
            return Mono.error(new ServerWebInputException("Invalid expiresAt."));
        }
        var roles = patSpec.getRoles();
        return hasSufficientRoles(authorities, roles)
            .filter(has -> has)
            .switchIfEmpty(
                Mono.error(() -> new ServerWebInputException("Insufficient roles."))
            )
            .map(has -> {
                var pat = new PersonalAccessToken();
                pat.setMetadata(new Metadata());
                if (patRequest.getMetadata() != null) {
                    var metadata = patRequest.getMetadata();
                    if (metadata.getName() != null) {
                        pat.getMetadata().setName(metadata.getName());
                    }
                    if (metadata.getGenerateName() != null) {
                        pat.getMetadata().setGenerateName(metadata.getGenerateName());
                    }
                    if (metadata.getLabels() != null) {
                        pat.getMetadata().setLabels(new HashMap<>());
                        pat.getMetadata().getLabels().putAll(metadata.getLabels());
                    }
                    if (metadata.getAnnotations() != null) {
                        pat.getMetadata().setAnnotations(new HashMap<>());
                        pat.getMetadata().getAnnotations()
                            .putAll(metadata.getAnnotations());
                    }
                    if (metadata.getFinalizers() != null) {
                        pat.getMetadata().setFinalizers(new HashSet<>());
                        pat.getMetadata().getFinalizers().addAll(metadata.getFinalizers());
                    }
                }
                if (pat.getMetadata().getGenerateName() == null) {
                    pat.getMetadata().setGenerateName("pat-" + username + "-");
                }
                pat.getSpec().setUsername(username);
                pat.getSpec().setName(patSpec.getName());
                pat.getSpec().setDescription(patSpec.getDescription());
                if (patSpec.getRoles() != null) {
                    pat.getSpec().setRoles(new ArrayList<>());
                    pat.getSpec().getRoles().addAll(patSpec.getRoles());
                }
                if (patSpec.getScopes() != null) {
                    pat.getSpec().setScopes(new ArrayList<>());
                    pat.getSpec().getScopes().addAll(patSpec.getScopes());
                }
                pat.getSpec().setExpiresAt(patSpec.getExpiresAt());
                pat.getSpec().setTokenId(idGenerator.generateId().toString());
                return pat;
            })
            .flatMap(client::create);
    }

    @Override
    public Mono<PersonalAccessToken> revoke(String patName, String username) {
        return get(patName, username)
            .filter(pat -> !pat.getSpec().isRevoked())
            .switchIfEmpty(Mono.error(
                () -> new ServerWebInputException("The token has been revoked before."))
            )
            .doOnNext(pat -> {
                pat.getSpec().setRevoked(true);
                pat.getSpec().setRevokesAt(clock.instant());
            })
            .flatMap(client::update);
    }

    @Override
    public Mono<PersonalAccessToken> restore(String patName, String username) {
        return get(patName, username)
            .filter(pat -> pat.getSpec().isRevoked())
            .switchIfEmpty(Mono.error(
                () -> new ServerWebInputException("The token has not been revoked before."))
            )
            .doOnNext(pat -> {
                pat.getSpec().setRevoked(false);
                pat.getSpec().setRevokesAt(null);
            })
            .flatMap(client::update);
    }

    @Override
    public Mono<PersonalAccessToken> delete(String patName, String username) {
        return get(patName, username)
            .flatMap(client::delete);
    }

    @Override
    public Mono<PersonalAccessToken> get(String patName, String username) {
        return client.fetch(PersonalAccessToken.class, patName)
            .filter(pat -> Objects.equals(pat.getSpec().getUsername(), username))
            .switchIfEmpty(Mono.error(() -> new NotFoundException(
                "The personal access token was not found or deleted."
            )));
    }

    @Override
    public Mono<String> generateToken(PersonalAccessToken pat) {
        return Mono.deferContextual(
                contextView -> {
                    var externalUrl = ServerWebExchangeContextFilter.getExchange(contextView)
                        .map(exchange -> this.externalUrl.getURL(exchange.getRequest()))
                        .orElse(null);
                    if (externalUrl == null) {
                        return Mono.error(new ServerWebInputException("Server web exchange is "
                            + "required"));
                    }
                    var claimsBuilder = JwtClaimsSet.builder()
                        .issuer(externalUrl.toString())
                        .id(pat.getSpec().getTokenId())
                        .subject(pat.getSpec().getUsername())
                        .issuedAt(clock.instant())
                        .claim("pat_name", pat.getMetadata().getName());
                    var expiresAt = pat.getSpec().getExpiresAt();
                    if (expiresAt != null) {
                        claimsBuilder.expiresAt(expiresAt);
                    }
                    var headerBuilder = JwsHeader.with(SignatureAlgorithm.RS256)
                        .keyId(this.keyId);
                    var jwt = jwtEncoder.encode(JwtEncoderParameters.from(
                        headerBuilder.build(),
                        claimsBuilder.build()));
                    return Mono.just(jwt);
                }
            )
            .map(jwt -> PersonalAccessToken.PAT_TOKEN_PREFIX + jwt.getTokenValue());
    }

    private Mono<Boolean> hasSufficientRoles(
        Collection<? extends GrantedAuthority> grantedAuthorities, List<String> requestRoles) {
        if (CollectionUtils.isEmpty(requestRoles)) {
            return Mono.just(true);
        }
        var grantedRoles = AuthorityUtils.authoritiesToRoles(grantedAuthorities);
        return roleService.contains(grantedRoles, requestRoles);
    }
}
