package run.halo.app.security.resolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import run.halo.app.exception.AuthenticationException;
import run.halo.app.model.entity.User;
import run.halo.app.security.authentication.Authentication;
import run.halo.app.security.context.SecurityContextHolder;
import run.halo.app.security.support.UserDetail;

import java.util.Optional;

/**
 * Authentication argument resolver.
 *
 * @author johnniang
 * @date 12/11/18
 */
@Slf4j
public class AuthenticationArgumentResolver implements HandlerMethodArgumentResolver {

    public AuthenticationArgumentResolver() {
        log.debug("Initializing AuthenticationArgumentResolver");
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> parameterType = parameter.getParameterType();
        return Authentication.class.isAssignableFrom(parameterType)
                || UserDetail.class.isAssignableFrom(parameterType)
                || User.class.isAssignableFrom(parameterType);
    }

    @Override
    @Nullable
    public Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer, NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) {
        log.debug("Handle AuthenticationArgument");

        Class<?> parameterType = parameter.getParameterType();

        Authentication authentication = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .orElseThrow(() -> new AuthenticationException("You haven't signed in yet"));

        if (Authentication.class.isAssignableFrom(parameterType)) {
            return authentication;
        } else if (UserDetail.class.isAssignableFrom(parameterType)) {
            return authentication.getDetail();
        } else if (User.class.isAssignableFrom(parameterType)) {
            return authentication.getDetail().getUser();
        }

        // Should never happen...
        throw new UnsupportedOperationException("Unknown parameter type: " + parameterType);
    }

}
