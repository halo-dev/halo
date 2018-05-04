package cc.ryanc.halo.web.controller.core;

/**
 * @author : RYAN0UP
 * @date : 2017/12/15
 * @version : 1.0
 * description:
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
        StringBuffer themeStr = new StringBuffer("themes/");
        themeStr.append(THEME);
        themeStr.append("/");
        return themeStr.append(pageName).toString();
    }
}
