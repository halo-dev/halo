package cc.ryanc.halo.web.controller.admin.base;

import cc.ryanc.halo.service.OptionService;
import cc.ryanc.halo.service.ThemeService;
import freemarker.template.Configuration;
import freemarker.template.TemplateModelException;
import org.springframework.beans.factory.annotation.Autowired;

import static cc.ryanc.halo.model.support.HaloConst.THEMES;

/**
 * Admin base Controller
 *
 * @author : RYAN0UP
 * @date : 2019/3/16
 */
public abstract class BaseController {

    @Autowired
    public Configuration configuration;

    @Autowired
    public OptionService optionService;

    @Autowired
    public ThemeService themeService;

    /**
     * Clear all caches
     */
    public void refreshCache() {
        try {
            THEMES.clear();
            THEMES = themeService.getThemes();
            configuration.setSharedVariable("options", optionService.listOptions());
        } catch (TemplateModelException e) {
            e.printStackTrace();
        }
    }
}
