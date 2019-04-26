package run.halo.app.controller.admin.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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
}
