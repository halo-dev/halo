package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.logging.Logger;
import cc.ryanc.halo.model.domain.*;
import cc.ryanc.halo.model.dto.CommentViewOutputDTO;
import cc.ryanc.halo.model.dto.PostViewOutputDTO;
import cc.ryanc.halo.model.enums.*;
import cc.ryanc.halo.model.params.PasswordResetParam;
import cc.ryanc.halo.model.support.JsonResult;
import cc.ryanc.halo.model.support.LogsRecord;
import cc.ryanc.halo.service.*;
import cc.ryanc.halo.utils.LocaleMessageUtil;
import cc.ryanc.halo.utils.MarkdownUtils;
import cc.ryanc.halo.web.controller.core.BaseController;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.text.StrBuilder;
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
import java.util.stream.Collectors;

import static cc.ryanc.halo.model.support.HaloConst.OPTIONS;
import static cc.ryanc.halo.model.support.HaloConst.USER_SESSION_KEY;

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

    private final static String RESET_PASSWORD_SESSION_KEY = "resetPasswordCode";

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

    @Autowired
    private MailService mailService;

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

        //附件数量
        model.addAttribute("mediaCount", attachmentService.count());

        //文章阅读总数
        final Long postViewsSum = postService.getPostViews();
        model.addAttribute("postViewsSum", postViewsSum);

        //查询最新的文章
        final List<PostViewOutputDTO> postsLatest = postService.findPostLatest()
                .stream()
                .map(post -> new PostViewOutputDTO().convertFrom(post))
                .collect(Collectors.toList());
        model.addAttribute("postsLatest", postsLatest);

        //查询最新的日志
        final List<Logs> logsLatest = logsService.findLogsLatest();
        model.addAttribute("logsLatest", logsLatest);

        //查询最新的评论
        final List<CommentViewOutputDTO> commentsLatest = commentService.findCommentsLatest()
                .stream()
                .map(comment -> new CommentViewOutputDTO().convertFrom(comment))
                .collect(Collectors.toList());
        model.addAttribute("commentsLatest", commentsLatest);

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
     * 重置密码
     *
     * @return String
     */
    @GetMapping(value = "/findPassword")
    public String findPassword() {
        return "admin/admin_findpassword";
    }

    /**
     * 发送重置密码邮件
     *
     * @param userName 用户名
     * @param email    邮箱
     * @return JsonResult
     */
    @PostMapping(value = "/sendResetPasswordEmail")
    @ResponseBody
    public JsonResult sendResetPasswordEmail(@RequestParam(value = "userName") String userName,
                                             @RequestParam(value = "email") String email,
                                             HttpSession session) {
        final User user = userService.findUser();
        if (StrUtil.isEmpty(userName) || StrUtil.isEmpty(email)) {
            return JsonResult.fail("请输入完整信息！");
        }
        if (!user.getUserEmail().equals(email) || !user.getUserName().equals(userName)) {
            return JsonResult.fail("用户名或电子邮箱错误，请确定你的身份！");
        }
        try {
            long time = System.currentTimeMillis();
            String randomString = RandomUtil.randomString(10);
            String code = SecureUtil.md5(time + randomString);
            StrBuilder url = new StrBuilder(OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp()));
            url.append("/admin/toResetPassword?code=");
            url.append(code);
            mailService.sendMail(user.getUserEmail(), "请根据该链接重置你的博客密码", "请点击该链接重置你的密码：" + url);
            session.setAttribute(RESET_PASSWORD_SESSION_KEY, code);
            return JsonResult.success("邮件发送成功，请登录您的邮箱进行下一步操作");
        } catch (Exception e) {
            log.error("Failed to send password email", e);
            return JsonResult.fail("邮件发送失败，请确定已经配置好了发信服务器信息");
        }
    }

    /**
     * 重置密码页面
     *
     * @param code code
     * @return String
     */
    @GetMapping(value = "/toResetPassword")
    public String toResetPassword(@RequestParam(value = "code", defaultValue = "") String code,
                                  Model model,
                                  HttpSession session) {
        String sessionCode = (String) session.getAttribute(RESET_PASSWORD_SESSION_KEY);

        model.addAttribute("isRight", StrUtil.equals(sessionCode, code));

        model.addAttribute("code", code);
        return "admin/admin_resetpassword";
    }

    /**
     * 重置密码
     *
     * @param resetParam password reset param
     * @return String
     */
    @PostMapping(value = "/resetPassword")
    @ResponseBody
    public JsonResult resetPassword(@RequestBody PasswordResetParam resetParam,
                                    HttpSession session) {
        final String sessionCode = (String) session.getAttribute(RESET_PASSWORD_SESSION_KEY);
        if (null == sessionCode || !StrUtil.equals(sessionCode, resetParam.getCode())) {
            return JsonResult.fail("不允许该操作！");
        }

        if (!StrUtil.equals(resetParam.getPassword(), resetParam.getDefinePassword())) {
            return JsonResult.fail("两次密码不一样！");
        }
        final User user = userService.findUser();
        user.setUserPass(SecureUtil.md5(resetParam.getPassword()));
        userService.update(user);
        userService.updateUserNormal();
        session.removeAttribute(RESET_PASSWORD_SESSION_KEY);
        return JsonResult.success("重置密码成功！");
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
