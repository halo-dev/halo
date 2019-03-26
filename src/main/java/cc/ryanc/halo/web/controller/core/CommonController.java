package cc.ryanc.halo.web.controller.core;

import cc.ryanc.halo.logging.Logger;
import cc.ryanc.halo.model.entity.User;
import cc.ryanc.halo.model.support.HaloConst;
import cc.ryanc.halo.service.ThemeService;
import cc.ryanc.halo.web.controller.content.base.BaseContentController;
import cn.hutool.core.text.StrBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.FileNotFoundException;

/**
 * Error page Controller
 *
 * @author : RYAN0UP
 * @date : 2017/12/26
 */
@Controller
public class CommonController implements ErrorController {

    private static final String ERROR_PATH = "/error";

    private static final String NOT_FROUND_TEMPLATE = "404.ftl";

    private static final String INTERNAL_ERROR_TEMPLATE = "500.ftl";

    private static final String ADMIN_URL = "/admin";

    private final Logger log = Logger.getLogger(getClass());

    @Autowired
    private ThemeService themeService;

    /**
     * Handle error
     *
     * @param request request
     * @return String
     */
    @GetMapping(value = ERROR_PATH)
    public String handleError(HttpServletRequest request, HttpSession session) {
        final Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");

        final String requestURI = request.getRequestURI();

        final User user = (User) session.getAttribute(HaloConst.USER_SESSION_KEY);

        log.error("Error path: [{}], status: [{}]", getErrorPath(), statusCode);

        // Get the exception
        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");

        if (throwable != null) {
            log.error("Captured an exception", throwable);

            if (StringUtils.startsWithIgnoreCase(throwable.getMessage(), "Could not resolve view with name '")) {
                // TODO May cause unreasoned problem
                // if Ftl was not found then redirect to /404
                if (requestURI.contains(ADMIN_URL) && null != user) {
                    return "redirect:/admin/404";
                } else {
                    return "redirect:/404";
                }
            }
        }
        if (requestURI.contains(ADMIN_URL) && null != user) {
            return "redirect:/admin/500";
        } else {
            return "redirect:/500";
        }
    }

    /**
     * Render 404 error page
     *
     * @return template path:
     */
    @GetMapping(value = "/admin/404")
    public String adminNotFround() {
        return "common/error/404";
    }

    /**
     * Render 500 error page
     *
     * @return template path:
     */
    @GetMapping(value = "/admin/500")
    public String adminInternalError() {
        return "common/error/500";
    }

    /**
     * Render 404 error page
     *
     * @return String
     */
    @GetMapping(value = "/404")
    public String contentNotFround() throws FileNotFoundException {
        if (!themeService.isTemplateExist(NOT_FROUND_TEMPLATE)) {
            return "common/error/404";
        }
        StrBuilder path = new StrBuilder("themes/");
        path.append(BaseContentController.THEME);
        path.append("/404");
        return path.toString();
    }

    /**
     * Render 500 error page
     *
     * @return template path:
     */
    @GetMapping(value = "/500")
    public String contentInternalError() throws FileNotFoundException {
        if (!themeService.isTemplateExist(INTERNAL_ERROR_TEMPLATE)) {
            return "common/error/500";
        }
        StrBuilder path = new StrBuilder("themes/");
        path.append(BaseContentController.THEME);
        path.append("/500");
        return path.toString();
    }

    /**
     * Returns the path of the error page.
     *
     * @return the error path
     */
    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }
}
