package run.halo.app.security.handler;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import run.halo.app.exception.AbstractHaloException;

/**
 * Authentication failure handler.
 *
 * @author johnniang
 */
public interface AuthenticationFailureHandler {

    /**
     * Calls when a user has been unsuccessfully authenticated.
     *
     * @param request http servlet request
     * @param response http servlet response
     * @param exception api exception
     * @throws IOException      io exception
     * @throws ServletException service exception
     */
    void onFailure(HttpServletRequest request, HttpServletResponse response,
        AbstractHaloException exception) throws IOException, ServletException;
}
