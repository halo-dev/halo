package cc.ryanc.halo.web.controller.content.base;

import cn.hutool.core.text.StrBuilder;

import static cc.ryanc.halo.model.support.HaloConst.DEFAULT_THEME_NAME;

/**
 * Content base Controller
 *
 * @author : RYAN0UP
 * @date : 2017/12/15
 */
public abstract class BaseContentController {

    /**
     * Default theme
     */
    @Deprecated
    public static String THEME = DEFAULT_THEME_NAME;

    /**
     * Render page by template name
     *
     * @param pageName pageName
     * @return template path
     */
    public String render(String pageName) {
        final StrBuilder themeStr = new StrBuilder("themes/");
        themeStr.append(THEME);
        themeStr.append("/");
        themeStr.append(pageName);
        return themeStr.toString();
    }

    /**
     * Redirect to 404
     *
     * @return redirect:/404
     */
    public String renderNotFound() {
        return "redirect:/404";
    }
}
