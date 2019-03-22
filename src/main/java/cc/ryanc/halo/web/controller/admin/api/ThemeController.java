package cc.ryanc.halo.web.controller.admin.api;

import cc.ryanc.halo.model.enums.BlogProperties;
import cc.ryanc.halo.model.support.Theme;
import cc.ryanc.halo.service.OptionService;
import cc.ryanc.halo.utils.ThemeUtils;
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

    public ThemeController(OptionService optionService,
                           Configuration configuration) {
        this.optionService = optionService;
        this.configuration = configuration;
    }

    /**
     * List all themes
     *
     * @return themes
     */
    @GetMapping
    @ApiOperation("List all themes")
    public List<Theme> listAll() {
        return ThemeUtils.getThemes();
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
        optionService.saveProperties(properties, "system");
        BaseContentController.THEME = themeName;
        configuration.setSharedVariable("themeName", themeName);
        configuration.setSharedVariable("options", optionService.listOptions());
    }
}
