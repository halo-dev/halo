package cc.ryanc.halo.web.controller.admin.api;

import cc.ryanc.halo.model.enums.BlogProperties;
import cc.ryanc.halo.model.enums.OptionSource;
import cc.ryanc.halo.model.support.Theme;
import cc.ryanc.halo.service.OptionService;
import cc.ryanc.halo.service.ThemeService;
import cc.ryanc.halo.web.controller.content.base.BaseContentController;
import freemarker.template.Configuration;
import freemarker.template.TemplateModelException;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
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
     * Active theme
     *
     * @param themeName theme name
     * @throws TemplateModelException TemplateModelException
     */
    @GetMapping(value = "active")
    @ApiOperation("Active theme")
    public void active(@RequestParam(name = "themeName", defaultValue = "anatole") String themeName) throws TemplateModelException {
        Map<BlogProperties, String> properties = new HashMap<>(1);
        properties.put(BlogProperties.THEME, themeName);
        optionService.saveProperties(properties, OptionSource.SYSTEM);
        BaseContentController.THEME = themeName;
        configuration.setSharedVariable("themeName", themeName);
        configuration.setSharedVariable("options", optionService.listOptions());
    }
}
