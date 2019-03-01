package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.logging.Logger;
import cc.ryanc.halo.model.domain.*;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.dto.LogsRecord;
import cc.ryanc.halo.model.enums.*;
import cc.ryanc.halo.service.*;
import cc.ryanc.halo.utils.LocaleMessageUtil;
import cc.ryanc.halo.utils.MarkdownUtils;
import cc.ryanc.halo.web.controller.core.BaseController;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HtmlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

import static cc.ryanc.halo.model.dto.HaloConst.OPTIONS;
import static cc.ryanc.halo.model.dto.HaloConst.USER_SESSION_KEY;

/**
 * <pre>
 *     后台首页控制器
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/12/5
 */
@Controller
@RequestMapping(value = "/admin")
public class AdminController extends BaseController {

    private final Logger log = Logger.getLogger(getClass());

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
    private CategoryService categoryService;

    @Autowired
    private TagService tagService;

    @Autowired
    private LocaleMessageUtil localeMessageUtil;

    /**
     * 请求后台页面
     *
     * @param model model
     * @return 模板路径admin/admin_index
     */
    @GetMapping(value = {"", "/index"})
    public String index(Model model) {

        //查询评论的条数
        final Long commentCount = commentService.count();
        model.addAttribute("commentCount", commentCount);

        //查询最新的文章
        final List<Post> postsLatest = postService.findPostLatest();
        model.addAttribute("postTopFive", postsLatest);

        //查询最新的日志
        final List<Logs> logsLatest = logsService.findLogsLatest();
        model.addAttribute("logs", logsLatest);

        //查询最新的评论
        final List<Comment> comments = commentService.findCommentsLatest();
        model.addAttribute("comments", comments);

        //附件数量
        model.addAttribute("mediaCount", attachmentService.count());

        //文章阅读总数
        final Long postViewsSum = postService.getPostViews();
        model.addAttribute("postViewsSum", postViewsSum);

        //成立天数
        final Date blogStart = DateUtil.parse(OPTIONS.get(BlogPropertiesEnum.BLOG_START.getProp()));
        final long hadDays = DateUtil.between(blogStart, DateUtil.date(), DateUnit.DAY);
        model.addAttribute("hadDays", hadDays);
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
        final User user = (User) session.getAttribute(USER_SESSION_KEY);
        //如果session存在，跳转到后台首页
        return user != null ? "redirect:/admin" : "admin/admin_login";
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
        final User aUser = userService.findUser();

        //首先判断是否已经被禁用已经是否已经过了10分钟
        Date loginLast = DateUtil.date();
        if (null != aUser.getLoginLast()) {
            loginLast = aUser.getLoginLast();
        }
        final Long between = DateUtil.between(loginLast, DateUtil.date(), DateUnit.MINUTE);
        if (StrUtil.equals(aUser.getLoginEnable(), TrueFalseEnum.FALSE.getDesc()) && (between < CommonParamsEnum.TEN.getValue())) {
            return JsonResult.fail(localeMessageUtil.getMessage("code.admin.login.disabled"));
        }

        //验证用户名和密码
        User user;
        if (Validator.isEmail(loginName)) {
            user = userService.userLoginByEmail(loginName, SecureUtil.md5(loginPwd));
        } else {
            user = userService.userLoginByName(loginName, SecureUtil.md5(loginPwd));
        }
        userService.updateUserLoginLast(DateUtil.date());

        //判断User对象是否相等
        if (Objects.equals(aUser, user)) {
            session.setAttribute(USER_SESSION_KEY, aUser);
            //重置用户的登录状态为正常
            userService.updateUserNormal();
            logsService.save(LogsRecord.LOGIN, LogsRecord.LOGIN_SUCCESS, request);
            log.info("User {} login succeeded.", aUser.getUserDisplayName());
            return JsonResult.success(localeMessageUtil.getMessage("code.admin.login.success"));
        } else {
            //更新失败次数
            final Integer errorCount = userService.updateUserLoginError();

            Integer limitCount = CommonParamsEnum.FIVE.getValue();

            log.error("Login failure count: [{}], but limit count: [{}]", errorCount, limitCount);

            //超过五次禁用账户
            if (errorCount >= limitCount) {
                log.error("Exceeded login limit. You have been locked permanently");
                userService.updateUserLoginEnable(TrueFalseEnum.FALSE.getDesc());
            }

            // Log login error detail
            logsService.save(LogsRecord.LOGIN, LogsRecord.LOGIN_ERROR + "[" + HtmlUtil.escape(loginName) + "," + HtmlUtil.escape(loginPwd) + "]", request);

            return JsonResult.fail(localeMessageUtil.getMessage("code.admin.login.failed", new Integer[]{5 - errorCount}));
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
        final User user = (User) session.getAttribute(USER_SESSION_KEY);
        session.removeAttribute(USER_SESSION_KEY);
        logsService.save(LogsRecord.LOGOUT, user.getUserName(), request);
        log.info("User {} has logged out", user.getUserName());
        return "redirect:/admin/login";
    }

    /**
     * 查看所有日志
     *
     * @param model model model
     * @return 模板路径admin/widget/_logs-all
     */
    @GetMapping(value = "/logs")
    public String logs(Model model, @PageableDefault Pageable pageable) {
        final Page<Logs> logs = logsService.listAll(pageable);
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
            logsService.removeAll();
        } catch (Exception e) {
            log.error("Clear log failed", e);
        }
        return "redirect:/admin";
    }

    /**
     * Halo关于页面
     *
     * @return 模板路径admin/admin_halo
     */
    @GetMapping(value = "/halo")
    public String halo() {
        return "admin/admin_halo";
    }

    /**
     * 获取一个Token
     *
     * @return JsonResult
     */
    @GetMapping(value = "/getToken")
    @ResponseBody
    public JsonResult getToken() {
        final String token = String.valueOf(System.currentTimeMillis() + RandomUtil.randomInt(Integer.MAX_VALUE));
        return JsonResult.success(HttpStatus.OK.getReasonPhrase(), SecureUtil.md5(token));
    }

    /**
     * 小工具
     *
     * @return String
     */
    @GetMapping(value = "/tools")
    public String tools() {
        return "admin/admin_tools";
    }

    /**
     * Markdown 导入页面
     *
     * @return String
     */
    @GetMapping(value = "/tools/markdownImport")
    public String markdownImport() {
        return "admin/widget/_markdown_import";
    }

    /**
     * Markdown 导入
     *
     * @param file    file
     * @param request request
     * @return JsonResult
     */
    @PostMapping(value = "/tools/markdownImport")
    @ResponseBody
    public JsonResult markdownImport(@RequestParam("file") MultipartFile file,
                                     HttpServletRequest request,
                                     HttpSession session) throws IOException {
        final User user = (User) session.getAttribute(USER_SESSION_KEY);
        final String markdown = IoUtil.read(file.getInputStream(), "UTF-8");
        final String content = MarkdownUtils.renderMarkdown(markdown);
        final Map<String, List<String>> frontMatters = MarkdownUtils.getFrontMatter(markdown);
        final Post post = new Post();
        final List<Tag> tags = new LinkedList<>();
        final List<Category> categories = new LinkedList<>();

        if (!CollectionUtils.isEmpty(frontMatters)) {
            // Iterate the map and inner list
            frontMatters.forEach((key, elementValue) -> elementValue.forEach(ele -> {
                switch (key) {
                    case "title":
                        post.setPostTitle(ele);
                        break;
                    case "date":
                        post.setPostDate(DateUtil.parse(ele));
                        break;
                    case "updated":
                        post.setPostUpdate(DateUtil.parse(ele));
                        break;
                    case "tags":
                        Tag tag = Optional.ofNullable(tagService.findTagByTagName(ele)).orElseGet(() -> {
                            Tag aTag = new Tag();
                            aTag.setTagName(ele);
                            aTag.setTagUrl(ele);
                            return tagService.create(aTag);
                        });
                        tags.add(tag);
                        break;
                    case "categories":
                        Category category = Optional.ofNullable(categoryService.findByCateName(ele)).orElseGet(() -> {
                            Category catg = new Category();
                            catg.setCateName(ele);
                            catg.setCateUrl(ele);
                            catg.setCateDesc(ele);
                            return categoryService.create(catg);
                        });
                        categories.add(category);
                        break;
                    default:
                        break;
                }
            }));
        }

        if (StrUtil.isBlank(post.getPostTitle())) {
            post.setPostTitle(file.getOriginalFilename());
        }

        post.setPostContentMd(markdown);
        post.setPostContent(content);
        post.setPostType(PostTypeEnum.POST_TYPE_POST.getDesc());
        post.setAllowComment(AllowCommentEnum.ALLOW.getCode());
        post.setUser(user);
        post.setTags(tags);
        post.setCategories(categories);
        post.setPostUrl(StrUtil.removeSuffix(file.getOriginalFilename(), ".md"));

        log.debug("Post you imported just now: [{}]", post);

        postService.create(post);
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode());
    }
}
