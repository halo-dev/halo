package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.Comment;
import cc.ryanc.halo.model.domain.Logs;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.domain.User;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.LogsRecord;
import cc.ryanc.halo.service.CommentService;
import cc.ryanc.halo.service.LogsService;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.service.UserService;
import cc.ryanc.halo.utils.HaloUtils;
import cc.ryanc.halo.web.controller.core.BaseController;
import cn.hutool.core.lang.Validator;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HtmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

/**
 * @author : RYAN0UP
 * @date : 2017/12/5
 * @version : 1.0
 * description: 后台首页控制器
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin")
public class AdminController extends BaseController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private LogsService logsService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private CommentService commentService;

    /**
     * 请求后台页面
     *
     * @param model   model
     * @param session session
     * @return 模板路径admin/admin_index
     */
    @GetMapping(value = {"", "/index"})
    public String index(Model model, HttpSession session) {
        //查询文章条数
        Integer postCount = postService.findAllPosts(HaloConst.POST_TYPE_POST).size();
        model.addAttribute("postCount", postCount);

        //查询评论的条数
        Integer commentCount = commentService.findAllComments().size();
        model.addAttribute("commentCount", commentCount);

        //查询最新的文章
        List<Post> postsLatest = postService.findPostLatest();
        model.addAttribute("postTopFive", postsLatest);

        //查询最新的日志
        List<Logs> logsLatest = logsService.findLogsLatest();
        model.addAttribute("logs", logsLatest);

        //查询最新的评论
        List<Comment> comments = commentService.findCommentsLatest();
        model.addAttribute("comments", comments);

        model.addAttribute("mediaCount", HaloConst.ATTACHMENTS.size());
        return "admin/admin_index";
    }

    /**
     * 处理跳转到登录页的请求
     *
     * @param session session
     * @return 模板路径admin/admin_login
     */
    @GetMapping(value = "/login")
    public String login(HttpSession session) {
        User user = (User) session.getAttribute(HaloConst.USER_SESSION_KEY);
        //如果session存在，跳转到后台首页
        if (null != user) {
            return "redirect:/admin";
        }
        return "admin/admin_login";
    }

    /**
     * 验证登录信息
     *
     * @param loginName 登录名：邮箱／用户名
     * @param loginPwd  loginPwd 密码
     * @param session   session session
     * @return String 登录状态
     */
    @PostMapping(value = "/getLogin")
    @ResponseBody
    public String getLogin(@ModelAttribute("loginName") String loginName,
                           @ModelAttribute("loginPwd") String loginPwd,
                           HttpSession session) {
        String status = "false";
        try {
            User aUser = userService.findUser();
            User user = null;
            if (StringUtils.equals(aUser.getLoginEnable(), "false")) {
                status = "disable";
            } else {
                if (Validator.isEmail(loginName)) {
                    user = userService.userLoginByEmail(loginName, SecureUtil.md5(loginPwd)).get(0);
                } else {
                    user = userService.userLoginByName(loginName, SecureUtil.md5(loginPwd)).get(0);
                }
                if (aUser == user) {
                    session.setAttribute(HaloConst.USER_SESSION_KEY, user);
                    //重置用户的登录状态为正常
                    userService.updateUserNormal();
                    userService.updateUserLoginLast(new Date());
                    logsService.saveByLogs(new Logs(LogsRecord.LOGIN, LogsRecord.LOGIN_SUCCESS, HaloUtils.getIpAddr(request), new Date()));
                    status = "true";
                }
            }
        } catch (Exception e) {
            Integer errorCount = userService.updateUserLoginError();
            if (errorCount >= 5) {
                userService.updateUserLoginEnable("false");
            }
            userService.updateUserLoginLast(new Date());
            logsService.saveByLogs(new Logs(LogsRecord.LOGIN, LogsRecord.LOGIN_ERROR + "[" + HtmlUtil.encode(loginName) + "," + HtmlUtil.encode(loginPwd) + "]", HaloUtils.getIpAddr(request), new Date()));
            log.error("登录失败！：{0}", e.getMessage());
        }
        return status;
    }

    /**
     * 退出登录 销毁session
     *
     * @param session session
     * @return 重定向到/admin/login
     */
    @GetMapping(value = "/logOut")
    public String logOut(HttpSession session) {
        User user = (User) session.getAttribute(HaloConst.USER_SESSION_KEY);
        logsService.saveByLogs(new Logs(LogsRecord.LOGOUT, user.getUserName(), HaloUtils.getIpAddr(request), new Date()));
        session.invalidate();
        log.info("用户[" + user.getUserName() + "]退出登录");
        return "redirect:/admin/login";
    }

    /**
     * 查看所有日志
     *
     * @param model model model
     * @param page  page 当前页码
     * @param size  size 每页条数
     * @return 模板路径admin/widget/_logs-all
     */
    @GetMapping(value = "/logs")
    public String logs(Model model,
                       @RequestParam(value = "page", defaultValue = "0") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Sort sort = new Sort(Sort.Direction.DESC, "logId");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Logs> logs = logsService.findAllLogs(pageable);
        model.addAttribute("logs", logs);
        return "admin/widget/_logs-all";
    }

    /**
     * 清除所有日志
     *
     * @return 重定向到/admin
     */
    @GetMapping(value = "/logs/clear")
    public String logsClear() {
        try {
            logsService.removeAllLogs();
        } catch (Exception e) {
            log.error("未知错误：" + e.getMessage());
        }
        return "redirect:/admin";
    }

    /**
     * 不可描述的页面
     *
     * @return 模板路径admin/admin_halo
     */
    @GetMapping(value = "/halo")
    public String halo() {
        return "admin/admin_halo";
    }
}
