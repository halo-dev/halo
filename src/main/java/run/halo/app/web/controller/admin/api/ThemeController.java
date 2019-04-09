package run.halo.app.web.controller.admin.api;

import freemarker.template.Configuration;
import freemarker.template.TemplateModelException;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.properties.PrimaryProperties;
import run.halo.app.model.support.BaseResponse;
import run.halo.app.model.support.ThemeFile;
import run.halo.app.model.support.ThemeProperty;
import run.halo.app.service.OptionService;
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

    private final OptionService optionService;

    private final Configuration configuration;

    private final ThemeService themeService;

    public ThemeController(OptionService optionService,
                           Configuration configuration,
                           ThemeService themeService) {
        this.optionService = optionService;
        this.configuration = configuration;
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
        return themeService.listThemeFolderBy(themeService.getActivatedTheme());
    }

    @GetMapping("files/content")
    public String getContentBy(@RequestParam(name = "path") String path) {
        return themeService.getTemplateContent(path);
    }

    @PutMapping("files/content")
    public void updateContentBy(@RequestParam(name = "path") String path,
                                @RequestParam(name = "content") String content) {
        themeService.saveTemplateContent(path, content);
    }

    @GetMapping("files/custom")
    public List<String> customTemplate() {
        return themeService.getCustomTpl(themeService.getActivatedTheme());
    }

    @PostMapping("{themeId}/activate")
    @ApiOperation("Active a theme")
    public void active(@RequestParam("themeId") String themeId) throws TemplateModelException {
        themeService.activeTheme(themeId);

        // TODO Check existence of the theme
        optionService.saveProperty(PrimaryProperties.THEME, themeId);
        configuration.setSharedVariable("themeName", themeId);
        configuration.setSharedVariable("options", optionService.listOptions());
    }

    @DeleteMapping("key/{key}")
    @ApiOperation("Deletes a theme")
    public void deleteBy(@PathVariable("key") String key) {
        themeService.deleteTheme(key);
    }

    @GetMapping("configurations")
    @ApiOperation("Fetches theme configuration")
    public BaseResponse<Object> fetchConfig() {
        return BaseResponse.ok(themeService.fetchConfig(themeService.getActivatedTheme()));
    }
}
