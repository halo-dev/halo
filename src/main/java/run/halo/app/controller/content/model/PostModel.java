package run.halo.app.controller.content.model;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.exception.ForbiddenException;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.PostMeta;
import run.halo.app.model.entity.Tag;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.support.HaloConst;
import run.halo.app.service.*;
import run.halo.app.utils.MarkdownUtils;

import java.util.List;

/**
 * @author ryan0up
 * @date 2020/1/7
 */
@Component
public class PostModel {

    private final PostService postService;

    private final ThemeService themeService;

    private final PostCategoryService postCategoryService;

    private final PostMetaService postMetaService;

    private final PostTagService postTagService;

    private final OptionService optionService;

    private final StringCacheStore cacheStore;

    public PostModel(PostService postService,
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

    public String post(Post post, String token, Model model) {

        if (post.getStatus().equals(PostStatus.INTIMATE) && StringUtils.isEmpty(token)) {
            String redirect = String.format("%s/archives/%s/password", optionService.getBlogBaseUrl(), post.getUrl());
            return "redirect:" + redirect;
        }

        if (!StringUtils.isEmpty(token)) {
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
}
