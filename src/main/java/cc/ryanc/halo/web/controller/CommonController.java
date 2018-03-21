package cc.ryanc.halo.web.controller;

import cc.ryanc.halo.model.dto.HaloConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
/**
 * @author : RYAN0UP
 * @date : 2017/12/26
 * @version : 1.0
 * description:
 */
@Slf4j
@Controller
public class CommonController implements ErrorController{

    private static final String ERROR_PATH = "/error";

    /**
     * 渲染404，500
     *
     * @param request request
     * @return string
     */
    @GetMapping(value = ERROR_PATH)
    public String handleError(HttpServletRequest request, Model model){
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        //设置选项
        model.addAttribute("options", HaloConst.OPTIONS);
        if(statusCode==404) {
            return "common/404";
        }else{
            return "common/500";
        }
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
