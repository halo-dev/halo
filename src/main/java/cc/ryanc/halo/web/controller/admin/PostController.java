package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.*;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.dto.LogsRecord;
import cc.ryanc.halo.model.enums.BlogPropertiesEnum;
import cc.ryanc.halo.model.enums.PostStatusEnum;
import cc.ryanc.halo.model.enums.PostTypeEnum;
import cc.ryanc.halo.model.enums.ResultCodeEnum;
import cc.ryanc.halo.service.CategoryService;
import cc.ryanc.halo.service.LogsService;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.service.TagService;
import cc.ryanc.halo.utils.HaloUtils;
import cc.ryanc.halo.utils.LocaleMessageUtil;
import cc.ryanc.halo.web.controller.core.BaseController;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
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
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

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
    private CategoryService categoryService;

    @Autowired
    private TagService tagService;

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
        return StringUtils.replaceAll(url, " ", "-");
    }

    /**
     * 处理后台获取文章列表的请求
     *
     * @param model model
     * @param page  当前页码
     * @param size  每页显示的条数
     * @return 模板路径admin/admin_post
     */
    @GetMapping
    public String posts(Model model,
                        @RequestParam(value = "status", defaultValue = "0") Integer status,
                        @RequestParam(value = "page", defaultValue = "0") Integer page,
                        @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Sort sort = new Sort(Sort.Direction.DESC, "postDate");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Post> posts = postService.findPostByStatus(status, PostTypeEnum.POST_TYPE_POST.getDesc(), pageable);
        model.addAttribute("posts", posts);
        model.addAttribute("publishCount", postService.getCountByStatus(PostStatusEnum.PUBLISHED.getCode()));
        model.addAttribute("draftCount", postService.getCountByStatus(PostStatusEnum.DRAFT.getCode()));
        model.addAttribute("trashCount", postService.getCountByStatus(PostStatusEnum.RECYCLE.getCode()));
        model.addAttribute("status", status);
        return "admin/admin_post";
    }

    /**
     * 模糊查询文章
     *
     * @param model   Model
     * @param keyword keyword 关键字
     * @param page    page 当前页码
     * @param size    size 每页显示条数
     * @return 模板路径admin/admin_post
     */
    @PostMapping(value = "/search")
    public String searchPost(Model model,
                             @RequestParam(value = "keyword") String keyword,
                             @RequestParam(value = "page", defaultValue = "0") Integer page,
                             @RequestParam(value = "size", defaultValue = "10") Integer size) {
        try {
            //排序规则
            Sort sort = new Sort(Sort.Direction.DESC, "postId");
            Pageable pageable = PageRequest.of(page, size, sort);
            model.addAttribute("posts", postService.searchPosts(keyword, pageable));
        } catch (Exception e) {
            log.error("未知错误：{}", e.getMessage());
        }
        return "admin/admin_post";
    }

    /**
     * 处理预览文章的请求
     *
     * @param postId 文章编号
     * @param model  model
     * @return 模板路径/themes/{theme}/post
     */
    @GetMapping(value = "/view")
    public String viewPost(@RequestParam("postId") Long postId, Model model) {
        Optional<Post> post = postService.findByPostId(postId);
        model.addAttribute("post", post.get());
        return this.render("post");
    }

    /**
     * 处理跳转到新建文章页面
     *
     * @return 模板路径admin/admin_editor
     */
    @GetMapping(value = "/new")
    public String newPost() {
        return "admin/admin_post_md_editor";
    }

    /**
     * 添加文章
     *
     * @param post     Post实体
     * @param cateList 分类列表
     * @param tagList  标签列表
     * @param session  session
     */
    @PostMapping(value = "/new/push")
    @ResponseBody
    public JsonResult pushPost(@ModelAttribute Post post, @RequestParam("cateList") List<String> cateList, @RequestParam("tagList") String tagList, HttpSession session) {
        User user = (User) session.getAttribute(HaloConst.USER_SESSION_KEY);
        String msg = localeMessageUtil.getMessage("code.admin.common.save-success");
        try {
            //提取摘要
            int postSummary = 50;
            if (StringUtils.isNotEmpty(HaloConst.OPTIONS.get(BlogPropertiesEnum.POST_SUMMARY.getProp()))) {
                postSummary = Integer.parseInt(HaloConst.OPTIONS.get(BlogPropertiesEnum.POST_SUMMARY.getProp()));
            }
            //文章摘要
            String summaryText = HtmlUtil.cleanHtmlTag(post.getPostContent());
            if (summaryText.length() > postSummary) {
                String summary = summaryText.substring(0, postSummary);
                post.setPostSummary(summary);
            } else {
                post.setPostSummary(summaryText);
            }
            //添加文章时，添加文章时间和修改文章时间为当前时间，修改文章时，只更新修改文章时间
            if (null != post.getPostId()) {
                Post oldPost = postService.findByPostId(post.getPostId()).get();
                post.setPostDate(oldPost.getPostDate());
                post.setPostUpdate(DateUtil.date());
                post.setPostViews(oldPost.getPostViews());
                msg = localeMessageUtil.getMessage("code.admin.common.update-success");
            } else {
                post.setPostDate(DateUtil.date());
                post.setPostUpdate(DateUtil.date());
            }
            post.setUser(user);
            List<Category> categories = categoryService.strListToCateList(cateList);
            post.setCategories(categories);
            if (StringUtils.isNotEmpty(tagList)) {
                List<Tag> tags = tagService.strListToTagList(StringUtils.deleteWhitespace(tagList));
                post.setTags(tags);
            }
            post.setPostUrl(urlFilter(post.getPostUrl()));
            //当没有选择文章缩略图的时候，自动分配一张内置的缩略图
            if (StringUtils.equals(post.getPostThumbnail(), BlogPropertiesEnum.DEFAULT_THUMBNAIL.getProp())) {
                post.setPostThumbnail("/static/images/thumbnail/thumbnail-" + RandomUtil.randomInt(1, 10) + ".jpg");
            }
            postService.saveByPost(post);
            logsService.saveByLogs(new Logs(LogsRecord.PUSH_POST, post.getPostTitle(), ServletUtil.getClientIP(request), DateUtil.date()));
            return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), msg);
        } catch (Exception e) {
            log.error("保存文章失败：{}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.save-failed"));
        }
    }


    /**
     * 自动保存文章为草稿
     *
     * @param postId        文章编号
     * @param postTitle     文章标题
     * @param postUrl       文章路径
     * @param postContentMd 文章内容
     * @param postType      文章类型
     * @param session       session
     * @return JsonResult
     */
    @PostMapping(value = "/new/autoPush")
    @ResponseBody
    public JsonResult autoPushPost(@RequestParam(value = "postId", defaultValue = "0") Long postId,
                                   @RequestParam(value = "postTitle") String postTitle,
                                   @RequestParam(value = "postUrl") String postUrl,
                                   @RequestParam(value = "postContentMd") String postContentMd,
                                   @RequestParam(value = "postType", defaultValue = "post") String postType,
                                   HttpSession session) {
        Post post = null;
        User user = (User) session.getAttribute(HaloConst.USER_SESSION_KEY);
        if (postId == 0) {
            post = new Post();
        } else {
            post = postService.findByPostId(postId).get();
        }
        try {
            if (StringUtils.isEmpty(postTitle)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                post.setPostTitle("草稿：" + dateFormat.format(DateUtil.date()));
            } else {
                post.setPostTitle(postTitle);
            }
            if (StringUtils.isEmpty(postUrl)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                post.setPostUrl(dateFormat.format(DateUtil.date()));
            } else {
                post.setPostUrl(postUrl);
            }
            post.setPostId(postId);
            post.setPostStatus(1);
            post.setPostContentMd(postContentMd);
            post.setPostType(postType);
            post.setPostDate(DateUtil.date());
            post.setPostUpdate(DateUtil.date());
            post.setUser(user);
        } catch (Exception e) {
            log.error("未知错误：{}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.save-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.save-success"), postService.saveByPost(post));
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
            log.info("编号为" + postId + "的文章已被移到回收站");
        } catch (Exception e) {
            log.error("删除文章到回收站失败：{}", e.getMessage());
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
            log.info("编号为" + postId + "的文章已改变为发布状态");
        } catch (Exception e) {
            log.error("发布文章失败：{}", e.getMessage());
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
            Optional<Post> post = postService.findByPostId(postId);
            postService.removeByPostId(postId);
            logsService.saveByLogs(new Logs(LogsRecord.REMOVE_POST, post.get().getPostTitle(), ServletUtil.getClientIP(request), DateUtil.date()));
        } catch (Exception e) {
            log.error("删除文章失败：{}", e.getMessage());
        }
        if (StringUtils.equals(PostTypeEnum.POST_TYPE_POST.getDesc(), postType)) {
            return "redirect:/admin/posts?status=2";
        }
        return "redirect:/admin/page";
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
        Optional<Post> post = postService.findByPostId(postId);
        model.addAttribute("post", post.get());
        return "admin/admin_post_md_editor";
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
            log.error("更新摘要失败：{}", e.getMessage());
            e.printStackTrace();
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.update-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.update-success"));
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
        Post post = postService.findByPostUrl(postUrl, PostTypeEnum.POST_TYPE_POST.getDesc());
        if (null != post) {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.url-is-exists"));
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
        if (StringUtils.isEmpty(baiduToken)) {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.post.no-baidu-token"));
        }
        String blogUrl = HaloConst.OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp());
        List<Post> posts = postService.findAllPosts(PostTypeEnum.POST_TYPE_POST.getDesc());
        StringBuilder urls = new StringBuilder();
        for (Post post : posts) {
            urls.append(blogUrl);
            urls.append("/archives/");
            urls.append(post.getPostUrl());
            urls.append("\n");
        }
        String result = HaloUtils.baiduPost(blogUrl, baiduToken, urls.toString());
        if (StringUtils.isEmpty(result)) {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.post.push-to-baidu-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.post.push-to-baidu-success"));
    }
}
