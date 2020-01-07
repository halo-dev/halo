package run.halo.app.controller.content;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import run.halo.app.model.entity.PostMeta;
import run.halo.app.model.entity.Tag;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.support.HaloConst;
import run.halo.app.model.vo.PostListVO;
import run.halo.app.service.*;
import run.halo.app.utils.MarkdownUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Blog archive page controller
 *
 * @author ryanwang
 * @author guqing
 * @author evanwang
 * @date 2019-03-17
 */
@Slf4j
@Controller
@RequestMapping(value = "/archives")
public class ContentArchiveController {

    private final PostService postService;

    private final ThemeService themeService;

    private final PostCategoryService postCategoryService;

    private final PostMetaService postMetaService;

    private final PostTagService postTagService;

    private final OptionService optionService;

    private final StringCacheStore cacheStore;

    public ContentArchiveController(PostService postService,
                                    ThemeService themeService,
                                    PostCategoryService postCategoryService,
                                    PostMetaService postMetaService,
                                    PostTagService postTagService,
                                    OptionService optionService,
                                    StringCacheStore cacheStore) {
        this.postService = postService;
        this.themeService = themeService;
        this.postCategoryService = postCategoryService;
        this.postMetaService = postMetaService;
        this.postTagService = postTagService;
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
     * @param url   post slug url.
     * @param token view token.
     * @param model model
     * @return template path: themes/{theme}/post.ftl
     */
    @GetMapping("{url}")
    public String post(@PathVariable("url") String url,
                       @RequestParam(value = "token", required = false) String token,
                       Model model) {
        Post post = postService.getByUrl(url);

        if (post.getStatus().equals(PostStatus.INTIMATE) && StringUtils.isEmpty(token)) {
            String redirect = String.format("%s/archives/%s/password", optionService.getBlogBaseUrl(), post.getUrl());
            return "redirect:" + redirect;
        }

        if (StringUtils.isEmpty(token)) {
            post = postService.getBy(PostStatus.PUBLISHED, url);
        } else {
            // verify token
            String cachedToken = cacheStore.getAny(token, String.class).orElseThrow(() -> new ForbiddenException("您没有该文章的访问权限"));
            if (!cachedToken.equals(token)) {
                throw new ForbiddenException("您没有该文章的访问权限");
            }
            post.setFormatContent(MarkdownUtils.renderHtml(post.getOriginalContent()));
        }
        postService.publishVisitEvent(post.getId());
        postService.getNextPost(post.getCreateTime()).ifPresent(nextPost -> model.addAttribute("nextPost", nextPost));
        postService.getPrePost(post.getCreateTime()).ifPresent(prePost -> model.addAttribute("prePost", prePost));

        List<Category> categories = postCategoryService.listCategoriesBy(post.getId());
        List<Tag> tags = postTagService.listTagsBy(post.getId());
        List<PostMeta> metas = postMetaService.listBy(post.getId());

        model.addAttribute("is_post", true);
        model.addAttribute("post", postService.convertToDetailVo(post));
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tags);
        model.addAttribute("metas", postMetaService.convertToMap(metas));

        // TODO,Will be deprecated
        model.addAttribute("comments", Page.empty());

        if (themeService.templateExists(ThemeService.CUSTOM_POST_PREFIX + post.getTemplate() + HaloConst.SUFFIX_FTL)) {
            return themeService.render(ThemeService.CUSTOM_POST_PREFIX + post.getTemplate());
        }

        return themeService.render("post");
    }

    @GetMapping(value = "{url}/password")
    public String password(@PathVariable("url") String url,
                           Model model) {
        model.addAttribute("url", url);
        return "common/template/post_password";
    }

    @PostMapping(value = "{url}/password")
    @CacheLock(traceRequest = true, expired = 2)
    public String password(@PathVariable("url") String url,
                           @RequestParam(value = "password") String password) {
        Post post = postService.getBy(PostStatus.INTIMATE, url);

        if (password.equals(post.getPassword())) {
            String token = IdUtil.simpleUUID();
            cacheStore.putAny(token, token, 10, TimeUnit.SECONDS);

            String redirect = String.format("%s/archives/%s?token=%s", optionService.getBlogBaseUrl(), post.getUrl(), token);
            return "redirect:" + redirect;
        } else {
            String redirect = String.format("%s/archives/%s/password", optionService.getBlogBaseUrl(), post.getUrl());
            return "redirect:" + redirect;
        }
    }
}
