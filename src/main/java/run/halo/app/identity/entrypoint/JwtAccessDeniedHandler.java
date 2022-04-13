package run.halo.app.identity.entrypoint;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.util.Assert;
import run.halo.app.infra.utils.HaloUtils;

/**
 * @author guqing
 * @date 2022-04-12
 */
@Slf4j
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private String errorPage;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
        AccessDeniedException accessDeniedException) throws IOException, ServletException {
        if (response.isCommitted()) {
            log.trace("Did not write to response since already committed");
            return;
        }
        if (this.errorPage == null) {
            log.debug("Responding with 403 status code");
            response.sendError(HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase());
            return;
        }
        // Put exception into request scope (perhaps of use to a view)
        request.setAttribute(WebAttributes.ACCESS_DENIED_403, accessDeniedException);
        // Set the 403 status code.
        response.setStatus(HttpStatus.FORBIDDEN.value());
        // forward to error page.
        if (log.isDebugEnabled()) {
            log.debug("Forwarding to [{}] with status code 403", this.errorPage);
        }
        if (HaloUtils.isAjaxRequest(request)) {
            response.getWriter().write("access denied!");
            return;
        }
        request.getRequestDispatcher(this.errorPage).forward(request, response);
    }

    public void setErrorPage(String errorPage) {
        Assert.isTrue(errorPage == null || errorPage.startsWith("/"),
            "errorPage must begin with '/'");
        this.errorPage = errorPage;
    }
}
