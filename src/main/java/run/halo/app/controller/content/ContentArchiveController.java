package run.halo.app.controller.content;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.cache.lock.CacheLock;
import run.halo.app.exception.ForbiddenException;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.Tag;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.vo.BaseCommentVO;
import run.halo.app.model.vo.PostListVO;
import run.halo.app.service.*;
import run.halo.app.utils.MarkdownUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Blog archive page controller
 *
 * @author ryanwang
 * @date : 2019-03-17
 */
@Slf4j
@Controller
@RequestMapping(value = "/archives")
public class ContentArchiveController {

    private final PostService postService;

    private final ThemeService themeService;

    private final PostCategoryService postCategoryService;

    private final PostTagService postTagService;

    private final PostCommentService postCommentService;

    private final OptionService optionService;

    private final StringCacheStore cacheStore;

    public ContentArchiveController(PostService postService,
                                    ThemeService themeService,
                                    PostCategoryService postCategoryService,
                                    PostTagService postTagService,
                                    PostCommentService postCommentService,
                                    OptionService optionService,
                                    StringCacheStore cacheStore) {
        this.postService = postService;
        this.themeService = themeService;
        this.postCategoryService = postCategoryService;
        this.postTagService = postTagService;
        this.postCommentService = postCommentService;
        this.optionService = optionService;
        this.cacheStore = cacheStore;
    }

    /**
     * Render post archives page.
     *
     * @param model model
     * @return template path : themes/{theme}/archives.ftl
     */
    @GetMapping
    public String archives(Model model) {
        return this.archives(model, 1, Sort.by(DESC, "createTime"));
    }

    /**
     * Render post archives page.
     *
     * @param model model
     * @return template path : themes/{theme}/archives.ftl
     */
    @GetMapping(value = "page/{page}")
    public String archives(Model model,
                           @PathVariable(value = "page") Integer page,
                           @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        Pageable pageable = PageRequest.of(page - 1, optionService.getPostPageSize(), sort);

        Page<Post> postPage = postService.pageBy(PostStatus.PUBLISHED, pageable);
        Page<PostListVO> postListVos = postService.convertToListVo(postPage);
        int[] pageRainbow = PageUtil.rainbow(page, postListVos.getTotalPages(), 3);

        model.addAttribute("is_archives", true);
        model.addAttribute("pageRainbow", pageRainbow);
        model.addAttribute("posts", postListVos);

        return themeService.render("archives");
    }

    /**
     * Render post page.
     *
     * @param url     post slug url.
     * @param preview preview
     * @param token   preview token
     * @param model   model
     * @return template path: themes/{theme}/post.ftl
     */
    @GetMapping("{url}")
    public String post(@PathVariable("url") String url,
                       @RequestParam(value = "preview", required = false, defaultValue = "false") boolean preview,
                       @RequestParam(value = "intimate", required = false, defaultValue = "false") boolean intimate,
                       @RequestParam(value = "token", required = false) String token,
                       @RequestParam(value = "cp", defaultValue = "1") Integer cp,
                       @SortDefault(sort = "createTime", direction = DESC) Sort sort,
                       Model model) {
        Post post;
        if (preview) {
            post = postService.getBy(PostStatus.DRAFT, url);
        } else if (intimate) {
            post = postService.getBy(PostStatus.INTIMATE, url);
        } else {
            post = postService.getBy(PostStatus.PUBLISHED, url);
        }

        // if this is a preview url.
        if (preview) {
            // render markdown to html when preview post
            post.setFormatContent(MarkdownUtils.renderHtml(post.getOriginalContent()));

            // verify token
            String cachedToken = cacheStore.getAny("preview-post-token-" + post.getId(), String.class).orElseThrow(() -> new ForbiddenException("该文章的预览链接不存在或已过期"));

            if (!cachedToken.equals(token)) {
                throw new ForbiddenException("该文章的预览链接不存在或已过期");
            }
        }

        // if this is a intimate url.
        if (intimate) {
            // verify token
            String cachedToken = cacheStore.getAny(token, String.class).orElseThrow(() -> new ForbiddenException("您没有该文章的访问权限"));
            if (!cachedToken.equals(token)) {
                throw new ForbiddenException("您没有该文章的访问权限");
            }
        }

        postService.getNextPost(post.getCreateTime()).ifPresent(nextPost -> model.addAttribute("nextPost", nextPost));
        postService.getPrePost(post.getCreateTime()).ifPresent(prePost -> model.addAttribute("prePost", prePost));

        List<Category> categories = postCategoryService.listCategoriesBy(post.getId());
        List<Tag> tags = postTagService.listTagsBy(post.getId());

        Page<BaseCommentVO> comments = postCommentService.pageVosBy(post.getId(), PageRequest.of(cp, optionService.getCommentPageSize(), sort));

        model.addAttribute("is_post", true);
        model.addAttribute("post", postService.convertToDetailVo(post));
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tags);
        model.addAttribute("comments", comments);

        if (preview) {
            // refresh timeUnit
            cacheStore.putAny("preview-post-token-" + post.getId(), token, 10, TimeUnit.MINUTES);
        }

        return themeService.render("post");
    }

    @GetMapping(value = "{url}/password")
    public String password(@PathVariable("url") String url,
                           Model model) {
        Post post = postService.getBy(PostStatus.INTIMATE, url);
        if (null == post) {
            throw new ForbiddenException("没有查询到该文章信息");
        }

        model.addAttribute("url", url);
        return "common/template/post_password";
    }

    @PostMapping(value = "{url}/password")
    @CacheLock
    public String password(@PathVariable("url") String url,
                           @RequestParam(value = "password") String password) {
        Post post = postService.getBy(PostStatus.INTIMATE, url);
        if (null == post) {
            throw new ForbiddenException("没有查询到该文章信息");
        }

        if (password.equals(post.getPassword())) {
            String token = IdUtil.simpleUUID();
            cacheStore.putAny(token, token, 10, TimeUnit.SECONDS);

            String redirect = String.format("%s/archives/%s?intimate=true&token=%s", optionService.getBlogBaseUrl(), post.getUrl(), token);
            return "redirect:" + redirect;
        } else {
            String redirect = String.format("%s/archives/%s/password", optionService.getBlogBaseUrl(), post.getUrl());
            return "redirect:" + redirect;
        }
    }
}
