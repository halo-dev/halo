package run.halo.app.controller.content.model;

import cn.hutool.core.util.PageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import run.halo.app.model.vo.AdjacentPostVO;
import run.halo.app.model.vo.PostListVO;
import run.halo.app.service.*;
import run.halo.app.utils.MarkdownUtils;

import java.util.List;

/**
 * Post Model
 *
 * @author ryanwang
 * @date 2020-01-07
 */
@Component
public class PostModel {

    private final PostService postService;

    private final ThemeService themeService;

    private final PostCategoryService postCategoryService;

    private final CategoryService categoryService;

    private final PostTagService postTagService;

    private final TagService tagService;

    private final PostMetaService postMetaService;

    private final OptionService optionService;

    private final StringCacheStore cacheStore;

    public PostModel(PostService postService,
                     ThemeService themeService,
                     PostCategoryService postCategoryService,
                     CategoryService categoryService,
                     PostMetaService postMetaService,
                     PostTagService postTagService,
                     TagService tagService,
                     OptionService optionService,
                     StringCacheStore cacheStore) {
        this.postService = postService;
        this.themeService = themeService;
        this.postCategoryService = postCategoryService;
        this.categoryService = categoryService;
        this.postMetaService = postMetaService;
        this.postTagService = postTagService;
        this.tagService = tagService;
        this.optionService = optionService;
        this.cacheStore = cacheStore;
    }

    public String content(Post post, String token, Model model) {

        if (post.getStatus().equals(PostStatus.INTIMATE) && StringUtils.isEmpty(token)) {
            model.addAttribute("slug", post.getSlug());
            return "common/template/post_password";
        }

        if (StringUtils.isEmpty(token)) {
            post = postService.getBy(PostStatus.PUBLISHED, post.getSlug());
        } else {
            // verify token
            String cachedToken = cacheStore.getAny(token, String.class).orElseThrow(() -> new ForbiddenException("您没有该文章的访问权限"));
            if (!cachedToken.equals(token)) {
                throw new ForbiddenException("您没有该文章的访问权限");
            }
            post.setFormatContent(MarkdownUtils.renderHtml(post.getOriginalContent()));
        }

        postService.publishVisitEvent(post.getId());

        AdjacentPostVO adjacentPostVO = postService.getAdjacentPosts(post);
        adjacentPostVO.getOptionalPrePost().ifPresent(prePost -> model.addAttribute("prePost", postService.convertToDetailVo(prePost)));
        adjacentPostVO.getOptionalNextPost().ifPresent(nextPost -> model.addAttribute("nextPost", postService.convertToDetailVo(nextPost)));

        List<Category> categories = postCategoryService.listCategoriesBy(post.getId());
        List<Tag> tags = postTagService.listTagsBy(post.getId());
        List<PostMeta> metas = postMetaService.listBy(post.getId());

        model.addAttribute("is_post", true);
        model.addAttribute("post", postService.convertToDetailVo(post));
        model.addAttribute("categories", categoryService.convertTo(categories));
        model.addAttribute("tags", tagService.convertTo(tags));
        model.addAttribute("metas", postMetaService.convertToMap(metas));

        // TODO,Will be deprecated
        model.addAttribute("comments", Page.empty());

        if (themeService.templateExists(
            ThemeService.CUSTOM_POST_PREFIX + post.getTemplate() + HaloConst.SUFFIX_FTL)) {
            return themeService.render(ThemeService.CUSTOM_POST_PREFIX + post.getTemplate());
        }

        return themeService.render("post");
    }

    public String list(Integer page, Model model, String decide, String template) {
        int pageSize = optionService.getPostPageSize();
        Pageable pageable = PageRequest
            .of(page >= 1 ? page - 1 : page, pageSize, postService.getPostDefaultSort());

        Page<Post> postPage = postService.pageBy(PostStatus.PUBLISHED, pageable);
        Page<PostListVO> posts = postService.convertToListVo(postPage);

        // TODO remove this variable
        int[] rainbow = PageUtil.rainbow(page, posts.getTotalPages(), 3);

        // Next page and previous page url.
        StringBuilder nextPageFullPath = new StringBuilder();
        StringBuilder prePageFullPath = new StringBuilder();

        if (optionService.isEnabledAbsolutePath()) {
            nextPageFullPath.append(optionService.getBlogBaseUrl());
            prePageFullPath.append(optionService.getBlogBaseUrl());
        }

        nextPageFullPath.append("/page/")
            .append(posts.getNumber() + 2)
            .append(optionService.getPathSuffix());

        if (posts.getNumber() == 1) {
            prePageFullPath.append("/");
        } else {
            prePageFullPath.append("/page/")
                .append(posts.getNumber())
                .append(optionService.getPathSuffix());
        }

        model.addAttribute(decide, true);
        model.addAttribute("posts", posts);
        model.addAttribute("rainbow", rainbow);
        model.addAttribute("pageRainbow", rainbow);
        model.addAttribute("nextPageFullPath", nextPageFullPath.toString());
        model.addAttribute("prePageFullPath", prePageFullPath.toString());
        return themeService.render(template);
    }
}
