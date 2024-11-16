package run.halo.app.security.preauth;

import reactor.core.publisher.Flux;
import run.halo.app.infra.properties.SecurityProperties.PasswordResetMethod;

/**
 * Password reset availability providers.
 *
 * @author johnniang
 * @since 2.20.0
 */
public interface PasswordResetAvailabilityProviders {

    /**
     * Get available password reset methods.
     *
     * @return available password reset methods
     */
    Flux<PasswordResetMethod> getAvailableMethods();

    /**
     * Get other available password reset methods.
     *
     * @param methodName method name
     * @return other available password reset methods
     */
    default Flux<PasswordResetMethod> getOtherAvailableMethods(String methodName) {
        return getAvailableMethods().filter(method -> !method.getName().equals(methodName));
    }

}
