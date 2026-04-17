package run.halo.app.security;

import static run.halo.app.security.SecurityConstant.USERNAME_PARAMETER_NAME;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.core.user.service.UserLoginOrLogoutProcessing;
import run.halo.app.security.authentication.oauth2.OAuth2LoginHandlerEnhancer;
import run.halo.app.security.authentication.rememberme.RememberMeServices;
import run.halo.app.security.device.DeviceService;

/**
 * A default implementation for {@link LoginHandlerEnhancer} to handle device management and
 * remember me.
 *
 * @author guqing
 * @since 2.17.0
 */
@Component
@RequiredArgsConstructor
class LoginHandlerEnhancerImpl implements LoginHandlerEnhancer {

    private final RememberMeServices rememberMeServices;

    private final DeviceService deviceService;

    private final LoginParameterRequestCache parameterRequestCache;

    private final OAuth2LoginHandlerEnhancer oauth2LoginHandlerEnhancer;

    private final UserLoginOrLogoutProcessing userLoginOrLogoutProcessing;

    @Override
    public Mono<Void> onLoginSuccess(ServerWebExchange exchange,
        Authentication successfulAuthentication) {
        return rememberMeServices.loginSuccess(exchange, successfulAuthentication)
            .then(deviceService.loginSuccess(exchange, successfulAuthentication))
            .then(oauth2LoginHandlerEnhancer.loginSuccess(exchange, successfulAuthentication))
            .then(userLoginOrLogoutProcessing.loginProcessing(successfulAuthentication.getName()))
            .then(parameterRequestCache.removeParameter(exchange, USERNAME_PARAMETER_NAME));
    }

    @Override
    public Mono<Void> onLoginFailure(ServerWebExchange exchange,
        AuthenticationException exception) {
        return parameterRequestCache.saveParameter(exchange, USERNAME_PARAMETER_NAME)
            .then(rememberMeServices.loginFail(exchange));
    }
}
