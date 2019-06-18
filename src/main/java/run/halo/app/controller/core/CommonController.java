package run.halo.app.controller.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.NestedServletException;
import run.halo.app.exception.HaloException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.service.ThemeService;
import run.halo.app.utils.FilenameUtils;

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

    private static final String ERROR_TEMPLATE = "error.ftl";

    private static final String DEFAULT_ERROR_PATH = "common/error/error";

    private final ThemeService themeService;

    private final ErrorProperties errorProperties;

    private final ErrorAttributes errorAttributes;

    public CommonController(ThemeService themeService,
                            ErrorAttributes errorAttributes,
                            ServerProperties serverProperties) {
        super(errorAttributes);
        this.themeService = themeService;
        this.errorAttributes = errorAttributes;
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

        handleCustomException(request);

        Map<String, Object> errorDetail = Collections.unmodifiableMap(getErrorAttributes(request, isIncludeStackTrace(request)));
        model.addAttribute("error", errorDetail);

        log.debug("Error detail: [{}]", errorDetail);

        if (status.equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            return contentInternalError();
        } else if (status.equals(HttpStatus.NOT_FOUND)) {
            return contentNotFound();
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
    public String contentNotFound() {
        if (themeService.templateExists(ERROR_TEMPLATE)) {
            return getActualTemplatePath(ERROR_TEMPLATE);
        }

        if (themeService.templateExists(NOT_FOUND_TEMPLATE)) {
            return getActualTemplatePath(NOT_FOUND_TEMPLATE);
        }

        return defaultErrorHandler();
    }

    /**
     * Render 500 error page
     *
     * @return template path:
     */
    @GetMapping(value = "/500")
    public String contentInternalError() {
        if (themeService.templateExists(ERROR_TEMPLATE)) {
            return getActualTemplatePath(ERROR_TEMPLATE);
        }

        if (themeService.templateExists(INTERNAL_ERROR_TEMPLATE)) {
            return getActualTemplatePath(INTERNAL_ERROR_TEMPLATE);
        }

        return defaultErrorHandler();
    }

    private String defaultErrorHandler() {
        return DEFAULT_ERROR_PATH;
    }

    private String getActualTemplatePath(@NonNull String template) {
        Assert.hasText(template, "FTL template must not be blank");

        StringBuilder path = new StringBuilder();
        path.append("themes/")
                .append(themeService.getActivatedTheme().getFolderName())
                .append('/')
                .append(FilenameUtils.getBasename(template));

        return path.toString();
    }

    /**
     * Handles custom exception, like HaloException.
     *
     * @param request http servlet request must not be null
     */
    private void handleCustomException(@NonNull HttpServletRequest request) {
        Assert.notNull(request, "Http servlet request must not be null");

        Object throwableObject = request.getAttribute("javax.servlet.error.exception");
        if (throwableObject == null) {
            return;
        }

        Throwable throwable = (Throwable) throwableObject;
        log.error("Captured an exception", throwable);

        if (throwable instanceof NestedServletException) {
            Throwable rootCause = ((NestedServletException) throwable).getRootCause();
            if (rootCause instanceof HaloException) {
                HaloException haloException = (HaloException) rootCause;
                request.setAttribute("javax.servlet.error.status_code", haloException.getStatus().value());
                request.setAttribute("javax.servlet.error.exception", rootCause);
                request.setAttribute("javax.servlet.error.message", haloException.getMessage());
            }
        } else if (StringUtils.startsWithIgnoreCase(throwable.getMessage(), "Could not resolve view with name '")) {
            request.setAttribute("javax.servlet.error.status_code", HttpStatus.NOT_FOUND.value());

            NotFoundException viewNotFound = new NotFoundException("该路径没有对应的模板");
            request.setAttribute("javax.servlet.error.exception", viewNotFound);
            request.setAttribute("javax.servlet.error.message", viewNotFound.getMessage());
        }

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
