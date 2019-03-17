package cc.ryanc.halo.web.controller.content.base;

import cc.ryanc.halo.logging.Logger;
import cn.hutool.core.text.StrBuilder;

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
    public static String THEME = "anatole";

    protected Logger log = Logger.getLogger(getClass());

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
