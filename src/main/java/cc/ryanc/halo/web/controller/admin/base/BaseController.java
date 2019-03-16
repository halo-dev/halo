package cc.ryanc.halo.web.controller.admin.base;

import cc.ryanc.halo.service.OptionService;
import cc.ryanc.halo.utils.LocaleMessageUtil;
import cc.ryanc.halo.utils.ThemeUtils;
import freemarker.template.Configuration;
import freemarker.template.TemplateModelException;

import static cc.ryanc.halo.model.support.HaloConst.OPTIONS;
import static cc.ryanc.halo.model.support.HaloConst.THEMES;

/**
 * Admin base Controller
 *
 * @author : RYAN0UP
 * @date : 2019/3/16
 */
public abstract class BaseController {

    public final Configuration configuration;

    public final OptionService optionService;

    public final LocaleMessageUtil localeMessageUtil;

    public BaseController(Configuration configuration,
                          OptionService optionService,
                          LocaleMessageUtil localeMessageUtil) {
        this.configuration = configuration;
        this.optionService = optionService;
        this.localeMessageUtil = localeMessageUtil;
    }

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
