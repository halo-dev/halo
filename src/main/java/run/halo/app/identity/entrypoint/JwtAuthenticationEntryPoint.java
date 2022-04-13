package run.halo.app.identity.entrypoint;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import run.halo.app.infra.utils.HaloUtils;

/**
 * @author guqing
 * @date 2022-04-12
 */
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException, ServletException {
        log.warn("Unauthorized error: {}", authException.getMessage());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        if (HaloUtils.isAjaxRequest(request)) {
            response.getWriter().write(HttpStatus.UNAUTHORIZED.getReasonPhrase());
            return;
        }
        response.sendError(HttpStatus.UNAUTHORIZED.value(),
            HttpStatus.UNAUTHORIZED.getReasonPhrase());
    }
}
