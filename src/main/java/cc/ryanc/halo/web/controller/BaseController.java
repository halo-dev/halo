package cc.ryanc.halo.web.controller;

import cc.ryanc.halo.model.domain.Comment;
import cc.ryanc.halo.model.domain.User;
import cc.ryanc.halo.service.CommentService;
import cc.ryanc.halo.service.OptionsService;
import cc.ryanc.halo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

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
    private CommentService commentService;

    @Autowired
    private OptionsService optionsService;

    @Autowired
    private UserService userService;

    /**
     * 根据主题名称渲染页面
     *
     * @param pageName pageName
     * @return 返回拼接好的模板路径
     */
    public String render(String pageName){
        StringBuffer themeStr = new StringBuffer("themes/");
        themeStr.append(THEME);
        themeStr.append("/");
        return themeStr.append(pageName).toString();
    }

    /**
     * 获取新评论
     *
     * @param session session
     */
    protected void getNewComments(HttpSession session){
        Sort sort = new Sort(Sort.Direction.DESC,"commentDate");
        Pageable pageable = new PageRequest(0,999,sort);
        Page<Comment> comments = commentService.findAllComments(1,pageable);
        session.removeAttribute("newComments");
        session.setAttribute("newComments",comments.getContent());
    }
}
