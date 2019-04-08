package run.halo.app.web.controller.admin.api;

import freemarker.template.Configuration;
import freemarker.template.TemplateModelException;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.enums.OptionSource;
import run.halo.app.model.properties.PrimaryProperties;
import run.halo.app.model.support.BaseResponse;
import run.halo.app.model.support.Theme;
import run.halo.app.model.support.ThemeFile;
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

    /**
     * List all themes
     *
     * @return themes
     */
    @GetMapping
    @ApiOperation("List all themes")
    public List<Theme> listAll() {
        return themeService.getThemes();
    }

    /**
     * List all of theme files.
     *
     * @return List<ThemeFile>
     */
    @GetMapping("files")
    public List<ThemeFile> listFiles() {
        return themeService.listThemeFolderBy(themeService.getTheme());
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
        return themeService.getCustomTpl(themeService.getTheme());
    }

    @PostMapping("active")
    @ApiOperation("Active a theme")
    public void active(String theme) throws TemplateModelException {
        // TODO Check existence of the theme
        optionService.saveProperty(PrimaryProperties.THEME, theme, OptionSource.SYSTEM);
        configuration.setSharedVariable("themeName", theme);
        configuration.setSharedVariable("options", optionService.listOptions());
    }

    @DeleteMapping("{key}")
    @ApiOperation("Deletes a theme")
    public void deleteBy(@PathVariable("key") String key) {
        themeService.deleteTheme(key);
    }

    @GetMapping("configurations")
    @ApiOperation("Fetches theme configuration")
    public BaseResponse<Object> fetchConfig() {
        return BaseResponse.ok(themeService.fetchConfig(themeService.getTheme()));
    }
}
