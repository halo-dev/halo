package cc.ryanc.halo.web.controller;

import cc.ryanc.halo.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import javax.servlet.http.HttpSession;

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
    public static String THEME = "halo";

    @Autowired
    private PostService postService;

    /**
     * 渲染页面
     * @param pageName pageName
     * @return 返回拼接好的模板路径
     */
    public String render(String pageName){
        return "themes/"+THEME+"/"+pageName;
    }

    protected void getNewComments(HttpSession session){
        session.setAttribute("postTopFive",postService.findPostLatest());
    }
}
