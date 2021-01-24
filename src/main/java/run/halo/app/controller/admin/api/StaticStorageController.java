package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.model.params.StaticContentParam;
import run.halo.app.model.support.StaticFile;
import run.halo.app.service.StaticStorageService;

/**
 * Static storage controller.
 *
 * @author ryanwang
 * @date 2019-12-06
 */
@RestController
@RequestMapping("/api/admin/statics")
public class StaticStorageController {

    private final StaticStorageService staticStorageService;

    public StaticStorageController(StaticStorageService staticStorageService) {
        this.staticStorageService = staticStorageService;
    }

    @GetMapping
    @ApiOperation("Lists static files")
    public List<StaticFile> list() {
        return staticStorageService.listStaticFolder();
    }

    @DeleteMapping
    @ApiOperation("Deletes file by relative path")
    public void deletePermanently(@RequestParam("path") String path) {
        staticStorageService.delete(path);
    }

    @PostMapping
    @ApiOperation("Creates a folder")
    public void createFolder(String basePath,
        @RequestParam("folderName") String folderName) {
        staticStorageService.createFolder(basePath, folderName);
    }

    @PostMapping("upload")
    @ApiOperation("Uploads static file")
    public void upload(String basePath,
        @RequestPart("file") MultipartFile file) {
        staticStorageService.upload(basePath, file);
    }

    @PostMapping("rename")
    @ApiOperation("Renames static file")
    public void rename(String basePath,
        String newName) {
        staticStorageService.rename(basePath, newName);
    }

    @PutMapping("files")
    @ApiOperation("Save static file")
    public void save(@RequestBody StaticContentParam param) {
        staticStorageService.save(param.getPath(), param.getContent());
    }
}
