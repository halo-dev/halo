package run.halo.app.web.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.support.BaseResponse;
import run.halo.app.model.support.ThemeFile;
import run.halo.app.model.support.ThemeProperty;
import run.halo.app.service.ThemeService;
import run.halo.app.service.ThemeSettingService;

import java.util.List;
import java.util.Map;

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

    private final ThemeSettingService themeSettingService;

    public ThemeController(ThemeService themeService,
                           ThemeSettingService themeSettingService) {
        this.themeService = themeService;
        this.themeSettingService = themeSettingService;
    }

    @GetMapping("{themeId}")
    @ApiOperation("Gets theme property by theme id")
    public ThemeProperty getBy(@PathVariable("themeId") String themeId) {
        return themeService.getThemeOfNonNullBy(themeId);
    }

    @GetMapping
    @ApiOperation("List all themes")
    public List<ThemeProperty> listAll() {
        return themeService.getThemes();
    }

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

    @PostMapping("{themeId}/activation")
    @ApiOperation("Activates a theme")
    public ThemeProperty active(@PathVariable("themeId") String themeId) {
        return themeService.activeTheme(themeId);
    }

    @GetMapping("activation")
    @ApiOperation("Gets activate theme")
    public ThemeProperty getActivateTheme() {
        return themeService.getThemeOfNonNullBy(themeService.getActivatedThemeId());
    }

    @GetMapping("activation/configurations")
    @ApiOperation("Fetches activated theme configuration")
    public BaseResponse<Object> fetchConfig() {
        return BaseResponse.ok(themeService.fetchConfig(themeService.getActivatedThemeId()));
    }

    @GetMapping("activation/settings")
    @ApiOperation("Lists activated theme settings")
    public Map<String, Object> listSettingsBy() {
        return themeSettingService.listAsMapBy(themeService.getActivatedThemeId());
    }

    @PostMapping("activation/settings")
    @ApiOperation("Saves theme settings")
    public void saveSettingsBy(@RequestBody Map<String, Object> settings) {
        themeSettingService.save(settings, themeService.getActivatedThemeId());
    }

    @GetMapping("{themeId}/configurations")
    @ApiOperation("Fetches theme configuration by theme id")
    public BaseResponse<Object> fetchConfig(@PathVariable("themeId") String themeId) {
        return BaseResponse.ok(themeService.fetchConfig(themeId));
    }

    @PostMapping("{themeId}/settings")
    @ApiOperation("Saves theme settings")
    public void saveSettingsBy(@PathVariable("themeId") String themeId,
                               @RequestBody Map<String, Object> settings) {
        themeSettingService.save(settings, themeId);
    }

    @DeleteMapping("{themeId}")
    @ApiOperation("Deletes a theme")
    public void deleteBy(@PathVariable("themeId") String themeId) {
        themeService.deleteTheme(themeId);
    }

}
