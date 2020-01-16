package run.halo.app.controller.content;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.exception.ServiceException;
import run.halo.app.model.entity.User;
import run.halo.app.model.properties.BlogProperties;
import run.halo.app.model.support.HaloConst;
import run.halo.app.service.OptionService;
import run.halo.app.service.UserService;
import run.halo.app.utils.HaloUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Main controller.
 *
 * @author ryanwang
 * @date 2019-04-23
 */
@Controller
public class MainController {

    /**
     * Index redirect uri.
     */
    private final static String INDEX_REDIRECT_URI = "index.html";

    /**
     * Install redirect uri.
     */
    private final static String INSTALL_REDIRECT_URI = INDEX_REDIRECT_URI + "#install";

    private final UserService userService;

    private final OptionService optionService;

    private final HaloProperties haloProperties;

    public MainController(UserService userService, OptionService optionService, HaloProperties haloProperties) {
        this.userService = userService;
        this.optionService = optionService;
        this.haloProperties = haloProperties;
    }

    @GetMapping("${halo.admin-path:admin}")
    public void admin(HttpServletResponse response) throws IOException {
        String adminIndexRedirectUri = HaloUtils.ensureBoth(haloProperties.getAdminPath(), HaloUtils.URL_SEPARATOR) + INDEX_REDIRECT_URI;
        response.sendRedirect(adminIndexRedirectUri);
    }

    @GetMapping("version")
    @ResponseBody
    public String version() {
        return HaloConst.HALO_VERSION;
    }

    @GetMapping("install")
    public void installation(HttpServletResponse response) throws IOException {
        String installRedirectUri = StringUtils.appendIfMissing(this.haloProperties.getAdminPath(), "/") + INSTALL_REDIRECT_URI;
        response.sendRedirect(installRedirectUri);
    }

    @GetMapping("avatar")
    public void avatar(HttpServletResponse response) throws IOException {
        User user = userService.getCurrentUser().orElseThrow(() -> new ServiceException("未查询到博主信息"));
        if (StringUtils.isNotEmpty(user.getAvatar())) {
            response.sendRedirect(HaloUtils.normalizeUrl(user.getAvatar()));
        }
    }

    @GetMapping("logo")
    public void logo(HttpServletResponse response) throws IOException {
        String blogLogo = optionService.getByProperty(BlogProperties.BLOG_LOGO).orElse("").toString();
        if (StringUtils.isNotEmpty(blogLogo)) {
            response.sendRedirect(HaloUtils.normalizeUrl(blogLogo));
        }
    }

    @GetMapping("favicon.ico")
    public void favicon(HttpServletResponse response) throws IOException {
        String favicon = optionService.getByProperty(BlogProperties.BLOG_FAVICON).orElse("").toString();
        if (StringUtils.isNotEmpty(favicon)) {
            response.sendRedirect(HaloUtils.normalizeUrl(favicon));
        }
    }
}
