package run.halo.app.web.controller.admin.api;

import freemarker.template.Configuration;
import freemarker.template.TemplateModelException;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.enums.OptionSource;
import run.halo.app.model.properties.PrimaryProperties;
import run.halo.app.model.properties.PropertyEnum;
import run.halo.app.model.support.BaseResponse;
import run.halo.app.model.support.HaloConst;
import run.halo.app.model.support.Theme;
import run.halo.app.model.support.ThemeFile;
import run.halo.app.service.OptionService;
import run.halo.app.service.ThemeService;

import java.util.HashMap;
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
        return themeService.listThemeFolderBy(HaloConst.ACTIVATED_THEME_NAME);
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
        return themeService.getCustomTpl(HaloConst.ACTIVATED_THEME_NAME);
    }

    /**
     * Active theme.
     *
     * @param theme theme name
     * @throws TemplateModelException TemplateModelException
     */
    @GetMapping(value = "active")
    @ApiOperation("Active theme")
    public void active(@RequestParam(name = "theme", defaultValue = "anatole") String theme) throws TemplateModelException {
        Map<PropertyEnum, String> properties = new HashMap<>(1);
        properties.put(PrimaryProperties.THEME, theme);
        // TODO Refactor: saveProperties => saveProperty
        optionService.saveProperties(properties, OptionSource.SYSTEM);
        HaloConst.ACTIVATED_THEME_NAME = theme;
        configuration.setSharedVariable("themeName", theme);
        configuration.setSharedVariable("options", optionService.listOptions());
    }

    /**
     * Deletes a theme.
     *
     * @param key theme key
     */
    @DeleteMapping("{key}")
    @ApiOperation("Deletes a theme")
    public void deleteBy(@PathVariable("key") String key) {
        themeService.deleteTheme(key);
    }

    @GetMapping("configurations")
    @ApiOperation("Fetches theme configuration")
    public BaseResponse<Object> fetchConfig(@RequestParam("name") String name) {
        return BaseResponse.ok(themeService.fetchConfig(name));
    }
}
