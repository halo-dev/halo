package run.halo.app.security.authentication.twofactor.totp;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.security.authentication.login.HaloUser;
import run.halo.app.security.authentication.login.UsernamePasswordHandler;
import run.halo.app.security.authentication.twofactor.TwoFactorAuthentication;

@Slf4j
public class TotpAuthenticationFilter extends AuthenticationWebFilter {

    public TotpAuthenticationFilter(ServerSecurityContextRepository securityContextRepository,
        TotpAuthService totpAuthService,
        ServerResponse.Context context,
        MessageSource messageSource) {
        super(new TwoFactorAuthManager(totpAuthService));

        setSecurityContextRepository(securityContextRepository);
        setRequiresAuthenticationMatcher(pathMatchers(HttpMethod.POST, "/login/2fa/totp"));
        setServerAuthenticationConverter(new TotpCodeAuthenticationConverter());

        var handler = new UsernamePasswordHandler(context, messageSource);
        setAuthenticationSuccessHandler(handler);
        setAuthenticationFailureHandler(handler);
    }

    private static class TotpCodeAuthenticationConverter implements ServerAuthenticationConverter {

        private final String codeParameter = "code";

        @Override
        public Mono<Authentication> convert(ServerWebExchange exchange) {
            // Check the request is authenticated before.
            return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(TwoFactorAuthentication.class::isInstance)
                .switchIfEmpty(Mono.error(
                    () -> new TwoFactorAuthException("MFA Authentication required.")))
                .flatMap(authentication -> exchange.getFormData())
                .handle((formData, sink) -> {
                    var codeStr = formData.getFirst(codeParameter);
                    if (StringUtils.isBlank(codeStr)) {
                        sink.error(new TwoFactorAuthException("Empty code parameter."));
                        return;
                    }
                    try {
                        var code = Integer.parseInt(codeStr);
                        sink.next(new TotpAuthenticationToken(code));
                    } catch (NumberFormatException e) {
                        sink.error(
                            new TwoFactorAuthException("Invalid code parameter " + codeStr + '.'));
                    }
                });
        }
    }

    private static class TwoFactorAuthException extends AuthenticationException {

        public TwoFactorAuthException(String msg, Throwable cause) {
            super(msg, cause);
        }

        public TwoFactorAuthException(String msg) {
            super(msg);
        }

    }

    private static class TwoFactorAuthManager implements ReactiveAuthenticationManager {

        private final TotpAuthService totpAuthService;

        private TwoFactorAuthManager(TotpAuthService totpAuthService) {
            this.totpAuthService = totpAuthService;
        }

        @Override
        public Mono<Authentication> authenticate(Authentication authentication) {
            // it should be TotpAuthenticationToken
            var code = (Integer) authentication.getCredentials();
            log.debug("Got TOTP code {}", code);
            // get user details
            return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .cast(TwoFactorAuthentication.class)
                .map(TwoFactorAuthentication::getPrevious)
                .<Authentication>handle((prevAuth, sink) -> {
                    var principal = prevAuth.getPrincipal();
                    if (!(principal instanceof HaloUser haloUser)) {
                        sink.error(new TwoFactorAuthException("Invalid MFA authentication."));
                        return;
                    }
                    var encryptedSecret =
                        haloUser.getDelegate().getSpec().getTotpEncryptedSecret();
                    if (StringUtils.isBlank(encryptedSecret)) {
                        sink.error(new TwoFactorAuthException("Empty secret configured"));
                        return;
                    }
                    var rawSecret = totpAuthService.decryptSecret(encryptedSecret);
                    var validated = totpAuthService.validateTotp(rawSecret, code);
                    if (!validated) {
                        sink.error(new TwoFactorAuthException("Invalid TOTP code " + code));
                        return;
                    }
                    sink.next(prevAuth);
                })
                .doOnNext(previousAuth -> {
                    if (log.isDebugEnabled()) {
                        log.debug("TOTP authentication for {} with code {} successfully.",
                            previousAuth.getName(), code);
                    }
                });
        }
    }
}
