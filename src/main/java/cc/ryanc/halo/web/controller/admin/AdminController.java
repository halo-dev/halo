package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.Comment;
import cc.ryanc.halo.model.domain.Logs;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.domain.User;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.dto.LogsRecord;
import cc.ryanc.halo.model.enums.CommonParamsEnum;
import cc.ryanc.halo.model.enums.ResultCodeEnum;
import cc.ryanc.halo.model.enums.TrueFalseEnum;
import cc.ryanc.halo.service.*;
import cc.ryanc.halo.utils.LocaleMessageUtil;
import cc.ryanc.halo.web.controller.core.BaseController;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.extra.servlet.ServletUtil;
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
 * <pre>
 *     后台首页控制器
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/12/5
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

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private LocaleMessageUtil localeMessageUtil;

    /**
     * 请求后台页面
     *
     * @param model   model
     * @param session session
     * @return 模板路径admin/admin_index
     */
    @GetMapping(value = {"", "/index"})
    public String index(Model model) {

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

        //附件数量
        model.addAttribute("mediaCount", attachmentService.findAllAttachments().size());

        //文章阅读总数
        Long postViewsSum = postService.getPostViews();
        model.addAttribute("postViewsSum", postViewsSum);
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
     * @return JsonResult JsonResult
     */
    @PostMapping(value = "/getLogin")
    @ResponseBody
    public JsonResult getLogin(@ModelAttribute("loginName") String loginName,
                               @ModelAttribute("loginPwd") String loginPwd,
                               HttpSession session) {
        //已注册账号，单用户，只有一个
        User aUser = userService.findUser();
        //首先判断是否已经被禁用已经是否已经过了10分钟
        Date loginLast = DateUtil.date();
        if (null != aUser.getLoginLast()) {
            loginLast = aUser.getLoginLast();
        }
        Long between = DateUtil.between(loginLast, DateUtil.date(), DateUnit.MINUTE);
        if (StringUtils.equals(aUser.getLoginEnable(), TrueFalseEnum.FALSE.getDesc()) && (between < CommonParamsEnum.TEN.getValue())) {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.login.disabled"));
        }
        //验证用户名和密码
        User user = null;
        if (Validator.isEmail(loginName)) {
            user = userService.userLoginByEmail(loginName, SecureUtil.md5(loginPwd));
        } else {
            user = userService.userLoginByName(loginName, SecureUtil.md5(loginPwd));
        }
        userService.updateUserLoginLast(DateUtil.date());
        //判断User对象是否相等
        if (ObjectUtil.equal(aUser, user)) {
            session.setAttribute(HaloConst.USER_SESSION_KEY, aUser);
            //重置用户的登录状态为正常
            userService.updateUserNormal();
            logsService.saveByLogs(new Logs(LogsRecord.LOGIN, LogsRecord.LOGIN_SUCCESS, ServletUtil.getClientIP(request), DateUtil.date()));
            log.info("用户[{}]登录成功。", aUser.getUserDisplayName());
            return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.login.success"));
        } else {
            //更新失败次数
            Integer errorCount = userService.updateUserLoginError();
            //超过五次禁用账户
            if (errorCount >= CommonParamsEnum.FIVE.getValue()) {
                userService.updateUserLoginEnable(TrueFalseEnum.FALSE.getDesc());
            }
            logsService.saveByLogs(
                    new Logs(
                            LogsRecord.LOGIN,
                            LogsRecord.LOGIN_ERROR + "[" + HtmlUtil.escape(loginName) + "," + HtmlUtil.escape(loginPwd) + "]",
                            ServletUtil.getClientIP(request),
                            DateUtil.date()
                    )
            );
            Object[] args = {(5 - errorCount)};
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.login.failed", args));
        }
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
        session.removeAttribute(HaloConst.USER_SESSION_KEY);
        logsService.saveByLogs(new Logs(LogsRecord.LOGOUT, user.getUserName(), ServletUtil.getClientIP(request), DateUtil.date()));
        log.info("用户[{}]退出登录", user.getUserName());
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
            log.error("清除日志失败：{}" + e.getMessage());
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
