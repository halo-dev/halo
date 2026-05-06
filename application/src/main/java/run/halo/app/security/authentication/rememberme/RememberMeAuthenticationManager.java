package run.halo.app.security.authentication.rememberme;

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
public class RememberMeAuthenticationManager
        implements ReactiveAuthenticationManager, InitializingBean, MessageSourceAware {

    private final CookieSignatureKeyResolver cookieSignatureKeyResolver;
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.justOrEmpty(authentication)
                .filter(RememberMeAuthenticationToken.class::isInstance)
                .cast(RememberMeAuthenticationToken.class)
                .flatMap(this::doAuthenticate);
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(this.messages, "A message source must be set");
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }

    private Mono<Authentication> doAuthenticate(RememberMeAuthenticationToken token) {
        return cookieSignatureKeyResolver
                .resolveSigningKey()
                .filter(key -> key.hashCode() == token.getKeyHash())
                .switchIfEmpty(
                        Mono.error(() -> new BadCredentialsException(badCredentialMessage())))
                .thenReturn(token);
    }

    private String badCredentialMessage() {
        return this.messages.getMessage(
                "RememberMeAuthenticationProvider.incorrectKey",
                "The presented RememberMeAuthenticationToken does not contain the expected key");
    }
}
