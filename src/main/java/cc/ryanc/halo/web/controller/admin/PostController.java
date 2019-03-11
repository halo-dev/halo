package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.domain.User;
import cc.ryanc.halo.model.dto.PostAdminOutputDTO;
import cc.ryanc.halo.model.dto.PostViewOutputDTO;
import cc.ryanc.halo.model.enums.BlogPropertiesEnum;
import cc.ryanc.halo.model.enums.PostStatusEnum;
import cc.ryanc.halo.model.enums.PostTypeEnum;
import cc.ryanc.halo.model.enums.ResultCodeEnum;
import cc.ryanc.halo.model.support.JsonResult;
import cc.ryanc.halo.model.support.LogsRecord;
import cc.ryanc.halo.service.LogsService;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.utils.BeanUtils;
import cc.ryanc.halo.utils.HaloUtils;
import cc.ryanc.halo.utils.LocaleMessageUtil;
import cc.ryanc.halo.utils.MarkdownUtils;
import cc.ryanc.halo.web.controller.core.BaseController;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static cc.ryanc.halo.model.support.HaloConst.OPTIONS;
import static cc.ryanc.halo.model.support.HaloConst.USER_SESSION_KEY;
import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * <pre>
 *     后台文章管理控制器
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/12/10
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/posts")
public class PostController extends BaseController {

    @Autowired
    private PostService postService;

    @Autowired
    private LogsService logsService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private LocaleMessageUtil localeMessageUtil;

    /**
     * 去除html，htm后缀，以及将空格替换成-
     *
     * @param url url
     * @return String
     */
    private static String urlFilter(String url) {
        if (null != url) {
            final boolean urlEndsWithHtmlPostFix = url.endsWith(".html") || url.endsWith(".htm");
            if (urlEndsWithHtmlPostFix) {
                return url.substring(0, url.lastIndexOf("."));
            }
        }
        return StrUtil.replace(url, " ", "-");
    }

    /**
     * 处理后台获取文章列表的请求
     *
     * @param model model
     * @return 模板路径admin/admin_post
     */
    @GetMapping
    public String posts(Model model,
                        @RequestParam(value = "status", defaultValue = "0") Integer status,
                        @RequestParam(value = "page", defaultValue = "0") Integer page,
                        @SortDefault.SortDefaults({
                                @SortDefault(sort = "postPriority", direction = DESC),
                                @SortDefault(sort = "postDate", direction = DESC)
                        }) Sort sort) {
        final Pageable pageable = PageRequest.of(page, 10, sort);
        final Page<PostAdminOutputDTO> posts = postService.findPostByStatus(status, PostTypeEnum.POST_TYPE_POST.getDesc(), pageable)
                .map(post -> new PostAdminOutputDTO().convertFrom(post));
        model.addAttribute("posts", posts);
        model.addAttribute("publishCount", postService.getCountByStatus(PostStatusEnum.PUBLISHED.getCode()));
        model.addAttribute("draftCount", postService.getCountByStatus(PostStatusEnum.DRAFT.getCode()));
        model.addAttribute("trashCount", postService.getCountByStatus(PostStatusEnum.RECYCLE.getCode()));
        model.addAttribute("status", status);
        return "admin/admin_post";
    }

    /**
     * 处理跳转到新建文章页面
     *
     * @return 模板路径admin/admin_editor
     */
    @GetMapping(value = "/write")
    public String writePost() {
        return "admin/admin_post_new";
    }

    /**
     * 跳转到编辑文章页面
     *
     * @param postId 文章编号
     * @param model  model
     * @return 模板路径admin/admin_editor
     */
    @GetMapping(value = "/edit")
    public String editPost(@RequestParam("postId") Long postId, Model model) {
        final Optional<Post> post = postService.fetchById(postId);
        model.addAttribute("post", post.orElse(new Post()));
        return "admin/admin_post_edit";
    }

    /**
     * 添加文章
     *
     * @param post     post
     * @param cateList 分类列表
     * @param tagList  标签
     * @param session  session
     */
    @PostMapping(value = "/save")
    @ResponseBody
    public JsonResult save(@ModelAttribute Post post,
                           @RequestParam("cateList") List<String> cateList,
                           @RequestParam("tagList") String tagList,
//                           @RequestParam("metas") List<PostMeta> metas,
                           HttpSession session) {
//        post.setPostMetas(metas);
        final User user = (User) session.getAttribute(USER_SESSION_KEY);
        try {
            post.setPostContent(MarkdownUtils.renderMarkdown(post.getPostContentMd()));
            post.setUser(user);
            post = postService.buildCategoriesAndTags(post, cateList, tagList);
            post.setPostUrl(urlFilter(post.getPostUrl()));
            if (StrUtil.isNotEmpty(post.getPostPassword())) {
                post.setPostPassword(SecureUtil.md5(post.getPostPassword()));
            }
            //当没有选择文章缩略图的时候，自动分配一张内置的缩略图
            if (StrUtil.equals(post.getPostThumbnail(), BlogPropertiesEnum.DEFAULT_THUMBNAIL.getProp())) {
                post.setPostThumbnail(OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp()) + "/static/halo-frontend/images/thumbnail/thumbnail-" + RandomUtil.randomInt(1, 11) + ".jpg");
            }
            postService.create(post);
            logsService.save(LogsRecord.PUSH_POST, post.getPostTitle(), request);
            return JsonResult.success(localeMessageUtil.getMessage("code.admin.common.save-success"));
        } catch (Exception e) {
            log.error("Save article failed: {}", e.getMessage());
            e.printStackTrace();
            return JsonResult.fail(localeMessageUtil.getMessage("code.admin.common.save-failed"));
        }
    }

    /**
     * 更新
     *
     * @param post     post
     * @param cateList 分类目录
     * @param tagList  标签
     * @return JsonResult
     */
    @PostMapping(value = "/update")
    @ResponseBody
    public JsonResult update(@ModelAttribute Post post,
                             @RequestParam("cateList") List<String> cateList,
                             @RequestParam("tagList") String tagList) {
        //old data
        final Post oldPost = postService.fetchById(post.getPostId()).orElse(new Post());
        BeanUtils.updateProperties(oldPost, post);
        post.setPostContent(MarkdownUtils.renderMarkdown(post.getPostContentMd()));
        if (null == post.getPostDate()) {
            post.setPostDate(new Date());
        }
        post = postService.buildCategoriesAndTags(post, cateList, tagList);
        if (StrUtil.isNotEmpty(post.getPostPassword())) {
            post.setPostPassword(SecureUtil.md5(post.getPostPassword()));
        }
        //当没有选择文章缩略图的时候，自动分配一张内置的缩略图
        if (StrUtil.equals(post.getPostThumbnail(), BlogPropertiesEnum.DEFAULT_THUMBNAIL.getProp())) {
            post.setPostThumbnail(OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp()) + "/static/halo-frontend/images/thumbnail/thumbnail-" + RandomUtil.randomInt(1, 11) + ".jpg");
        }
        post = postService.create(post);
        if (null != post) {
            return JsonResult.success(localeMessageUtil.getMessage("code.admin.common.update-success"));
        } else {
            return JsonResult.fail(localeMessageUtil.getMessage("code.admin.common.update-failed"));
        }
    }

    /**
     * 处理移至回收站的请求
     *
     * @param postId 文章编号
     * @return 重定向到/admin/posts
     */
    @GetMapping(value = "/throw")
    public String moveToTrash(@RequestParam("postId") Long postId, @RequestParam("status") Integer status) {
        try {
            postService.updatePostStatus(postId, PostStatusEnum.RECYCLE.getCode());
            log.info("Article number {} has been moved to the recycle bin", postId);
        } catch (Exception e) {
            log.error("Deleting article to recycle bin failed: {}", e.getMessage());
        }
        return "redirect:/admin/posts?status=" + status;
    }

    /**
     * 处理文章为发布的状态
     *
     * @param postId 文章编号
     * @return 重定向到/admin/posts
     */
    @GetMapping(value = "/revert")
    public String moveToPublish(@RequestParam("postId") Long postId,
                                @RequestParam("status") Integer status) {
        try {
            postService.updatePostStatus(postId, PostStatusEnum.PUBLISHED.getCode());
            log.info("Article number {} has been changed to release status", postId);
        } catch (Exception e) {
            log.error("Publishing article failed: {}", e.getMessage());
        }
        return "redirect:/admin/posts?status=" + status;
    }

    /**
     * 处理删除文章的请求
     *
     * @param postId 文章编号
     * @return 重定向到/admin/posts
     */
    @GetMapping(value = "/remove")
    public String removePost(@RequestParam("postId") Long postId, @RequestParam("postType") String postType) {
        try {
            final Optional<Post> post = postService.fetchById(postId);
            postService.removeById(postId);
            logsService.save(LogsRecord.REMOVE_POST, post.get().getPostTitle(), request);
        } catch (Exception e) {
            log.error("Delete article failed: {}", e.getMessage());
        }
        if (StrUtil.equals(PostTypeEnum.POST_TYPE_POST.getDesc(), postType)) {
            return "redirect:/admin/posts?status=2";
        }
        return "redirect:/admin/page";
    }

    /**
     * 置顶/取消置顶文章
     *
     * @param postId   postId
     * @param priority priority
     * @return JsonResult
     */
    @GetMapping(value = "/topPost")
    public String topPost(@RequestParam("postId") Long postId,
                          @RequestParam("priority") Integer priority) {
        Post post = postService.getById(postId);
        post.setPostPriority(priority);
        postService.update(post);
        return "redirect:/admin/posts";
    }

    /**
     * 更新所有摘要
     *
     * @param postSummary 文章摘要字数
     * @return JsonResult
     */
    @GetMapping(value = "/updateSummary")
    @ResponseBody
    public JsonResult updateSummary(@RequestParam("postSummary") Integer postSummary) {
        try {
            postService.updateAllSummary(postSummary);
        } catch (Exception e) {
            log.error("Update summary failed: {}", e.getMessage());
            e.printStackTrace();
            return JsonResult.fail(localeMessageUtil.getMessage("code.admin.common.update-failed"));
        }
        return JsonResult.success(localeMessageUtil.getMessage("code.admin.common.update-success"));
    }

    /**
     * 验证文章路径是否已经存在
     *
     * @param postUrl 文章路径
     * @return JsonResult
     */
    @GetMapping(value = "/checkUrl")
    @ResponseBody
    public JsonResult checkUrlExists(@RequestParam("postUrl") String postUrl) {
        postUrl = urlFilter(postUrl);
        final Post post = postService.findByPostUrl(postUrl, PostTypeEnum.POST_TYPE_POST.getDesc());
        if (null != post) {
            return JsonResult.fail(localeMessageUtil.getMessage("code.admin.common.url-is-exists"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), "");
    }

    /**
     * 将所有文章推送到百度
     *
     * @param baiduToken baiduToken
     * @return JsonResult
     */
    @GetMapping(value = "/pushAllToBaidu")
    @ResponseBody
    public JsonResult pushAllToBaidu(@RequestParam("baiduToken") String baiduToken) {
        if (StrUtil.isBlank(baiduToken)) {
            return JsonResult.fail(localeMessageUtil.getMessage("code.admin.post.no-baidu-token"));
        }
        final String blogUrl = OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp());
        final List<PostViewOutputDTO> posts = postService.findAll(PostTypeEnum.POST_TYPE_POST.getDesc())
                .stream()
                .map(post -> (PostViewOutputDTO) new PostViewOutputDTO().convertFrom(post))
                .collect(Collectors.toList());
        final StringBuilder urls = new StringBuilder();
        for (PostViewOutputDTO post : posts) {
            urls.append(blogUrl);
            urls.append("/archives/");
            urls.append(post.getPostUrl());
            urls.append("\n");
        }
        final String result = HaloUtils.baiduPost(blogUrl, baiduToken, urls.toString());
        if (StrUtil.isEmpty(result)) {
            return JsonResult.fail(localeMessageUtil.getMessage("code.admin.post.push-to-baidu-failed"));
        }
        return JsonResult.success(localeMessageUtil.getMessage("code.admin.post.push-to-baidu-success"));
    }

    @InitBinder
    public void initBinder(ServletRequestDataBinder binder) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(sdf, true));
    }
}
