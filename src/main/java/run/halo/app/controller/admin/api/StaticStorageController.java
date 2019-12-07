package run.halo.app.controller.admin.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.model.support.StaticFile;
import run.halo.app.service.StaticStorageService;

import java.util.List;

/**
 * @author ryan0up
 * @date 2019/12/6
 */
@RestController
@RequestMapping("/api/admin/statics")
public class StaticStorageController {

    private final StaticStorageService staticStorageService;

    public StaticStorageController(StaticStorageService staticStorageService) {
        this.staticStorageService = staticStorageService;
    }

    @GetMapping
    public List<StaticFile> list() {
        return staticStorageService.listStaticFolder();
    }
}
