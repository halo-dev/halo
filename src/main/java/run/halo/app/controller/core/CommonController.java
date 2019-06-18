package run.halo.app.controller.core;

import cn.hutool.core.text.StrBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import run.halo.app.service.ThemeService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;

/**
 * Error page Controller
 *
 * @author ryanwang
 * @date : 2017/12/26
 */
@Slf4j
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class CommonController extends AbstractErrorController {

    private static final String NOT_FOUND_TEMPLATE = "404.ftl";

    private static final String INTERNAL_ERROR_TEMPLATE = "500.ftl";

    private static final String ERROR_TEMPLATE = "common/error/error";

    private final ThemeService themeService;

    private final ErrorProperties errorProperties;

    public CommonController(ThemeService themeService,
                            ErrorAttributes errorAttributes,
                            ServerProperties serverProperties) {
        super(errorAttributes);
        this.themeService = themeService;
        this.errorProperties = serverProperties.getError();
    }

    /**
     * Handle error
     *
     * @param request request
     * @return String
     */
    @GetMapping
    public String handleError(HttpServletRequest request, HttpServletResponse response, Model model) {
        HttpStatus status = getStatus(request);

        log.error("Error path: [{}], status: [{}]", getErrorPath(), status);

        // Get the exception
        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");

        if (throwable != null) {
            log.error("Captured an exception", throwable);

            if (StringUtils.startsWithIgnoreCase(throwable.getMessage(), "Could not resolve view with name '")) {
                // TODO May cause unknown-reason problem
                // if Ftl was not found then redirect to /404
                return contentNotFround();
            }
        }

        Map<String, Object> errorDetail = Collections.unmodifiableMap(getErrorAttributes(request, isIncludeStackTrace(request)));
        model.addAttribute("error", errorDetail);

        log.debug("Error detail: [{}]", errorDetail);

        if (status.equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            return contentInternalError();
        } else if (status.equals(HttpStatus.NOT_FOUND)) {
            return contentNotFround();
        } else {
            return defaultErrorHandler();
        }
    }

    /**
     * Render 404 error page
     *
     * @return String
     */
    @GetMapping(value = "/404")
    public String contentNotFround() {
        if (!themeService.templateExists(NOT_FOUND_TEMPLATE)) {
            return defaultErrorHandler();
        }
        StrBuilder path = new StrBuilder("themes/");
        path.append(themeService.getActivatedTheme().getFolderName());
        path.append("/404");
        return path.toString();
    }

    /**
     * Render 500 error page
     *
     * @return template path:
     */
    @GetMapping(value = "/500")
    public String contentInternalError() {
        if (!themeService.templateExists(INTERNAL_ERROR_TEMPLATE)) {
            return defaultErrorHandler();
        }

        StrBuilder path = new StrBuilder("themes/");
        path.append(themeService.getActivatedTheme().getFolderName());
        path.append("/500");
        return path.toString();
    }

    public String defaultErrorHandler() {
        return ERROR_TEMPLATE;
    }

    /**
     * Returns the path of the error page.
     *
     * @return the error path
     */
    @Override
    public String getErrorPath() {
        return this.errorProperties.getPath();
    }

    /**
     * Determine if the stacktrace attribute should be included.
     *
     * @param request the source request
     * @return if the stacktrace attribute should be included
     */
    protected boolean isIncludeStackTrace(HttpServletRequest request) {
        ErrorProperties.IncludeStacktrace include = errorProperties.getIncludeStacktrace();
        if (include == ErrorProperties.IncludeStacktrace.ALWAYS) {
            return true;
        }
        if (include == ErrorProperties.IncludeStacktrace.ON_TRACE_PARAM) {
            return getTraceParameter(request);
        }
        return false;
    }
}
