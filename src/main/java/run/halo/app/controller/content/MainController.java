package run.halo.app.controller.content;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import run.halo.app.exception.ServiceException;
import run.halo.app.model.entity.User;
import run.halo.app.model.properties.BlogProperties;
import run.halo.app.model.support.HaloConst;
import run.halo.app.service.OptionService;
import run.halo.app.service.UserService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Admin page.
 *
 * @author ryanwang
 * @date : 2019-04-23
 */
@Controller
public class MainController {

    private final UserService userService;

    private final OptionService optionService;

    public MainController(UserService userService, OptionService optionService) {
        this.userService = userService;
        this.optionService = optionService;
    }

    @GetMapping("/admin")
    public String admin() {
        return "redirect:/admin/index.html";
    }

    @GetMapping("/install")
    public String installation() {
        return "redirect:/admin/index.html#install";
    }

    @GetMapping("/version")
    @ResponseBody
    public String version() {
        return HaloConst.HALO_VERSION;
    }

    @GetMapping("/avatar")
    public void avatar(HttpServletResponse response) throws IOException {
        User user = userService.getCurrentUser().orElseThrow(() -> new ServiceException("找不到博主信息"));
        if (StringUtils.isNotEmpty(user.getAvatar())) {
            response.sendRedirect(user.getAvatar());
        }
    }

    @GetMapping("/logo")
    public void logo(HttpServletResponse response) throws IOException {
        String blogLogo = optionService.getByProperty(BlogProperties.BLOG_LOGO).orElse("").toString();
        if (StringUtils.isNotEmpty(blogLogo)) {
            response.sendRedirect(blogLogo);
        }
    }

    @GetMapping("/favicon.ico")
    public void favicon(HttpServletResponse response) throws IOException {
        String favicon = optionService.getByProperty(BlogProperties.BLOG_FAVICON).orElse("").toString();
        if (StringUtils.isNotEmpty(favicon)) {
            response.sendRedirect(favicon);
        }
    }
}
