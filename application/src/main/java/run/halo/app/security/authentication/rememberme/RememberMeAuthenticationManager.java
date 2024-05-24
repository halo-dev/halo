package run.halo.app.security.authentication.rememberme;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RememberMeAuthenticationManager implements ReactiveAuthenticationManager,
    InitializingBean, MessageSourceAware {

    private final CookieSignatureKeyResolver cookieSignatureKeyResolver;
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        if (authentication instanceof RememberMeAuthenticationToken rememberMeAuthenticationToken) {
            return doAuthenticate(rememberMeAuthenticationToken);
        }
        return Mono.empty();
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(this.messages, "A message source must be set");
    }

    @Override
    public void setMessageSource(@NonNull MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }

    private Mono<Authentication> doAuthenticate(RememberMeAuthenticationToken token) {
        return cookieSignatureKeyResolver.resolveSigningKey()
            .flatMap(key -> {
                if (key.hashCode() != token.getKeyHash()) {
                    return Mono.error(new BadCredentialsException(badCredentialMessage()));
                }
                return Mono.just(token);
            });
    }

    private String badCredentialMessage() {
        return this.messages.getMessage("RememberMeAuthenticationProvider.incorrectKey",
            "The presented RememberMeAuthenticationToken does not contain the expected key");
    }
}
