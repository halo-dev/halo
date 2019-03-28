package cc.ryanc.halo.security.handler;

import cc.ryanc.halo.exception.HaloException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Authentication failure handler.
 *
 * @author johnniang
 */
public class AdminAuthenticationFailureHandler extends DefaultAuthenticationFailureHandler {

    public AdminAuthenticationFailureHandler(boolean productionEnv, ObjectMapper objectMapper) {
        super(productionEnv, objectMapper);
    }

    @Override
    public void onFailure(HttpServletRequest request, HttpServletResponse response, HaloException exception) throws IOException, ServletException {
        // TODO handler the admin authentication failure.
        super.onFailure(request, response, exception);
    }
}
