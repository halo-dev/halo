package run.halo.app.security.handler;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import run.halo.app.exception.AbstractHaloException;
import run.halo.app.exception.NotInstallException;

/**
 * Content authentication failure handler.
 *
 * @author johnniang
 * @date 19-5-6
 */
public class ContentAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onFailure(HttpServletRequest request, HttpServletResponse response,
        AbstractHaloException exception) throws IOException, ServletException {
        if (exception instanceof NotInstallException) {
            response.sendRedirect(request.getContextPath() + "/install");
            return;
        }

        // Forward to error
        request.getRequestDispatcher(request.getContextPath() + "/error")
            .forward(request, response);
    }
}
