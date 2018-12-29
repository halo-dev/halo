package cc.ryanc.halo.web.controller.core;

import cn.hutool.core.text.StrBuilder;

/**
 * <pre>
 *     Controller抽象类
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/12/15
 */
public abstract class BaseController {

    /**
     * 定义默认主题
     */
    public static String THEME = "anatole";

    /**
     * 根据主题名称渲染页面
     *
     * @param pageName pageName
     * @return 返回拼接好的模板路径
     */
    public String render(String pageName) {
        final StrBuilder themeStr = new StrBuilder("themes/");
        themeStr.append(THEME);
        themeStr.append("/");
        return themeStr.append(pageName).toString();
    }

    /**
     * 渲染404页面
     *
     * @return redirect:/404
     */
    public String renderNotFound() {
        return "redirect:/404";
    }
}
