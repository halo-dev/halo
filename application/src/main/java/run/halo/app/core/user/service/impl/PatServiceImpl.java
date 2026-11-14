package run.halo.app.core.user.service.impl;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.hash.Hashing;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.*;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.CollectionUtils;
import org.springframework.util.IdGenerator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.user.service.PatService;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.infra.exception.UnsatisfiedAttributeValueException;
import run.halo.app.security.PersonalAccessToken;
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

    private final AuthenticationTrustResolver authTrustResolver = new AuthenticationTrustResolverImpl();

    private final ReactiveUserDetailsService userDetailsService;

    private final SecureRandom secureRandom = new SecureRandom();

    private Clock clock;

    public PatServiceImpl(
            RoleService roleService, ReactiveExtensionClient client, ReactiveUserDetailsService userDetailsService) {
        this.roleService = roleService;
        this.client = client;
        this.userDetailsService = userDetailsService;
        this.clock = Clock.systemUTC();
        idGenerator = new AlternativeJdkIdGenerator();
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
                .switchIfEmpty(Mono.error(
                        () -> new UnsatisfiedAttributeValueException("problemDetail.authentication.required")))
                .flatMap(auth -> create(patRequest, auth.getName(), auth.getAuthorities()));
    }

    @Override
    public Mono<PersonalAccessToken> create(PersonalAccessToken patRequest, String username) {
        return userDetailsService
                .findByUsername(username)
                .flatMap(userDetails -> create(patRequest, username, userDetails.getAuthorities()));
    }

    private Mono<PersonalAccessToken> create(
            PersonalAccessToken patRequest, String username, Collection<? extends GrantedAuthority> authorities) {
        var patSpec = patRequest.getSpec();
        // preflight check
        var expiresAt = patSpec.getExpiresAt();
        if (expiresAt != null && expiresAt.isBefore(clock.instant())) {
            return Mono.error(new UnsatisfiedAttributeValueException("problemDetail.pat.invalidExpiry"));
        }
        var roles = patSpec.getRoles();
        return hasSufficientRoles(authorities, roles)
                .filter(has -> has)
                .switchIfEmpty(
                        Mono.error(() -> new UnsatisfiedAttributeValueException("problemDetail.pat.insufficientRoles")))
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
                            pat.getMetadata().getAnnotations().putAll(metadata.getAnnotations());
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
                .switchIfEmpty(
                        Mono.error(() -> new UnsatisfiedAttributeValueException("problemDetail.pat.alreadyRevoked")))
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
                .switchIfEmpty(Mono.error(() -> new UnsatisfiedAttributeValueException("problemDetail.pat.notRevoked")))
                .doOnNext(pat -> {
                    pat.getSpec().setRevoked(false);
                    pat.getSpec().setRevokesAt(null);
                })
                .flatMap(client::update);
    }

    @Override
    public Mono<PersonalAccessToken> delete(String patName, String username) {
        return get(patName, username).flatMap(client::delete);
    }

    @Override
    public Mono<PersonalAccessToken> get(String patName, String username) {
        return client.fetch(PersonalAccessToken.class, patName)
                .filter(pat -> Objects.equals(pat.getSpec().getUsername(), username))
                .switchIfEmpty(Mono.error(() -> new NotFoundException(
                        "problemDetail.pat.notFound", null, "The personal access token was not found or deleted.")));
    }

    @Override
    public Mono<String> generateToken(PersonalAccessToken pat) {
        // SecureRandom may block on some platforms (e.g. when seeded from /dev/random), so offload
        // secret generation to the boundedElastic scheduler to keep the event loop non-blocking.
        return Mono.fromCallable(this::generateSecret)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(secret -> {
                    // Only the hash of the secret is persisted; the plain secret is returned once.
                    pat.getSpec().setTokenId(hashSecret(secret));
                    return client.update(pat)
                            .thenReturn(buildToken(pat.getMetadata().getName(), secret));
                });
    }

    private String generateSecret() {
        var bytes = new byte[24];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Hash the token secret for storage and comparison.
     *
     * @param secret the plain token secret
     * @return the SHA-256 hash of the secret, hex encoded
     */
    static String hashSecret(String secret) {
        return Hashing.sha256().hashString(secret, UTF_8).toString();
    }

    /**
     * Build the opaque token string for the given PAT name and secret.
     *
     * @param patName metadata.name of the PAT
     * @param secret the plain token secret
     * @return the token in the form of {@code pat_<base64url(patName:secret)>}
     */
    static String buildToken(String patName, String secret) {
        var encoded = Base64.getUrlEncoder().withoutPadding().encodeToString((patName + ':' + secret).getBytes(UTF_8));
        return PersonalAccessToken.PAT_TOKEN_PREFIX + encoded;
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
