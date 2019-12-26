package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.model.support.StaticPageFile;
import run.halo.app.service.StaticPageService;

import java.util.List;

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

    @GetMapping
    @ApiOperation("List static page files.")
    public List<StaticPageFile> list() {
        return staticPageService.listFile();
    }

    @GetMapping("generate")
    @ApiOperation("Generate static page files.")
    public void generate() {
        staticPageService.generate();
    }

    @GetMapping("deploy")
    @ApiOperation("Deploy static page to remove platform")
    public void deploy() {
        staticPageService.deploy();
    }
}
