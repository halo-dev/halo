package run.halo.app.security.preauth;

import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.properties.SecurityProperties;
import run.halo.app.infra.properties.SecurityProperties.PasswordResetMethod;

/**
 * Default password reset availability providers.
 *
 * @author johnniang
 * @since 2.20.0
 */
@Component
public class DefaultPasswordResetAvailabilityProviders
    implements PasswordResetAvailabilityProviders {

    private final SecurityProperties securityProperties;

    private final List<PasswordResetAvailabilityProvider> providers;

    public DefaultPasswordResetAvailabilityProviders(HaloProperties haloProperties,
        ObjectProvider<PasswordResetAvailabilityProvider> providers) {
        this.securityProperties = haloProperties.getSecurity();
        this.providers = providers.orderedStream().toList();
    }

    @Override
    public Flux<PasswordResetMethod> getAvailableMethods() {
        return Flux.fromIterable(securityProperties.getPasswordResetMethods())
            .filterWhen(method -> providers.stream()
                .filter(provider -> provider.support(method.getName()))
                .findFirst()
                .map(provider -> provider.isAvailable(method))
                .orElseGet(() -> Mono.just(false))
            );
    }
}
