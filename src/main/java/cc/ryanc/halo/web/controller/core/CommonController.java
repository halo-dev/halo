package cc.ryanc.halo.web.controller.core;

import cc.ryanc.halo.logging.Logger;
import cc.ryanc.halo.model.enums.CommonParamsEnum;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * <pre>
 *     错误页面控制器
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/12/26
 */
@Controller
public class CommonController implements ErrorController {

    private final Logger log = Logger.getLogger(getClass());

    private static final String ERROR_PATH = "/error";

    /**
     * 渲染404，500
     *
     * @param request request
     * @return String
     */
    @GetMapping(value = ERROR_PATH)
    public String handleError(HttpServletRequest request) {
        final Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");

        log.error("Error path: [{}], status: [{}]", request.getRequestURI(), statusCode);

        // Get the exception
        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");

        if (StringUtils.startsWithIgnoreCase(throwable.getMessage(), "Could not resolve view with name '")) {
            // TODO May cause unreasoned problem
            // if Ftl was not found then redirect to /404
            return "redirect:/404";
        }

        if (statusCode.equals(CommonParamsEnum.NOT_FOUND.getValue())) {
            return "redirect:/404";
        } else {
            return "redirect:/500";
        }
    }

    /**
     * 渲染404页面
     *
     * @return String
     */
    @GetMapping(value = "/404")
    public String fourZeroFour() {
        return "common/error/404";
    }

    /**
     * 渲染500页面
     *
     * @return String
     */
    @GetMapping(value = "/500")
    public String fiveZeroZero() {
        return "common/error/500";
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
