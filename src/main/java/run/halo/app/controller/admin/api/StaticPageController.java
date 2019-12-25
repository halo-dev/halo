package run.halo.app.controller.admin.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.service.StaticPageService;

/**
 * @author ryan0up
 * @date 2019/12/25
 */
@RestController
@RequestMapping("/api/admin/static_page")
public class StaticPageController {

    private final StaticPageService staticPageService;

    public StaticPageController(StaticPageService staticPageService) {
        this.staticPageService = staticPageService;
    }

    @GetMapping("generate")
    public void generate() {
        staticPageService.generate();
    }
}
