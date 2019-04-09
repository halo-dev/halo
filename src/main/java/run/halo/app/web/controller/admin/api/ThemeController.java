package run.halo.app.web.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.support.BaseResponse;
import run.halo.app.model.support.ThemeFile;
import run.halo.app.model.support.ThemeProperty;
import run.halo.app.service.ThemeService;

import java.util.List;

/**
 * Theme controller.
 *
 * @author : RYAN0UP
 * @date : 2019/3/20
 */
@RestController
@RequestMapping("/admin/api/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping("{themeId}")
    @ApiOperation("Gets theme property by theme id")
    public ThemeProperty getBy(@PathVariable("themeId") String themeId) {
        return themeService.getThemeOfNonNullBy(themeId);
    }

    /**
     * List all themes
     *
     * @return themes
     */
    @GetMapping
    @ApiOperation("List all themes")
    public List<ThemeProperty> listAll() {
        return themeService.getThemes();
    }

    /**
     * List all of theme files.
     *
     * @return List<ThemeFile>
     */
    @GetMapping("files")
    public List<ThemeFile> listFiles() {
        return themeService.listThemeFolderBy(themeService.getActivatedThemeId());
    }

    @GetMapping("files/content")
    public String getContentBy(@RequestParam(name = "path") String path) {
        return themeService.getTemplateContent(path);
    }

    @PutMapping("files/content")
    public void updateContentBy(@RequestParam(name = "path") String path,
                                @RequestParam(name = "content") String content) {
        // TODO Refactor the params to body
        themeService.saveTemplateContent(path, content);
    }

    @GetMapping("files/custom")
    public List<String> customTemplate() {
        return themeService.getCustomTpl(themeService.getActivatedThemeId());
    }

    @PostMapping("{themeId}/activate")
    @ApiOperation("Activates a theme")
    public ThemeProperty active(@RequestParam("themeId") String themeId) {
        return themeService.activeTheme(themeId);
    }

    @GetMapping("activate")
    @ApiOperation("Gets activate theme")
    public ThemeProperty getActivateTheme() {
        return themeService.getThemeOfNonNullBy(themeService.getActivatedThemeId());
    }

    @GetMapping("activate/configurations")
    @ApiOperation("Fetches theme configuration")
    public BaseResponse<Object> fetchConfig() {
        return BaseResponse.ok(themeService.fetchConfig(themeService.getActivatedThemeId()));
    }

    @GetMapping("{themeId}/configurations")
    @ApiOperation("Fetches theme configuration by theme id")
    public BaseResponse<Object> fetchConfig(@PathVariable("themeId") String themeId) {
        return BaseResponse.ok(themeService.fetchConfig(themeId));
    }

    @DeleteMapping("{themeId}")
    @ApiOperation("Deletes a theme")
    public void deleteBy(@PathVariable("themeId") String themeId) {
        themeService.deleteTheme(themeId);
    }

}
