package run.halo.app.controller.error;

import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.util.NestedServletException;
import run.halo.app.exception.AbstractHaloException;

/**
 * Default error controller.
 *
 * @author johnniang
 */
@Component
public class DefaultErrorController extends BasicErrorController {

    public DefaultErrorController(
        ErrorAttributes errorAttributes,
        ServerProperties serverProperties,
        List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, serverProperties.getError(), errorViewResolvers);
    }

    @Override
    protected HttpStatus getStatus(HttpServletRequest request) {
        var status = super.getStatus(request);
        // deduce status
        var exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        if (exception instanceof NestedServletException) {
            var nse = (NestedServletException) exception;
            if (nse.getCause() instanceof AbstractHaloException) {
                status = ((AbstractHaloException) nse.getCause()).getStatus();
                // reset status
                request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, status.value());
                request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, nse.getCause());
                request.setAttribute(RequestDispatcher.ERROR_MESSAGE, nse.getCause().getMessage());
                request.setAttribute(RequestDispatcher.ERROR_EXCEPTION_TYPE,
                    nse.getCause().getClass());
            }
        }
        return status;
    }
}
