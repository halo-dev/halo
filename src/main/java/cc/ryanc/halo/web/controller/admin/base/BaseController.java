package cc.ryanc.halo.web.controller.admin.base;

import cc.ryanc.halo.service.OptionService;
import cc.ryanc.halo.utils.LocaleMessageUtil;
import cc.ryanc.halo.utils.ThemeUtils;
import freemarker.template.Configuration;
import freemarker.template.TemplateModelException;
import org.springframework.beans.factory.annotation.Autowired;

import static cc.ryanc.halo.model.support.HaloConst.OPTIONS;
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
    public LocaleMessageUtil localeMessageUtil;

    /**
     * Clear all caches
     */
    public void refreshCache() {
        try {
            OPTIONS.clear();
            THEMES.clear();
            OPTIONS = optionService.listOptions();
            THEMES = ThemeUtils.getThemes();
            configuration.setSharedVariable("options", OPTIONS);
        } catch (TemplateModelException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get message with code
     *
     * @param code code
     * @return String
     */
    public String localeMessage(String code) {
        return localeMessageUtil.getMessage(code);
    }

    /**
     * Get message with code and params
     *
     * @param code code
     * @param args args
     * @return String
     */
    public String localeMessage(String code, Object[] args) {
        return localeMessageUtil.getMessage(code, args);
    }
}
