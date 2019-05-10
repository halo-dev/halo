package run.halo.app.controller.content;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import run.halo.app.model.support.HaloConst;

/**
 * Admin page.
 *
 * @author ryanwang
 * @date : 2019-04-23
 */
@Controller
public class MainController {


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
}
