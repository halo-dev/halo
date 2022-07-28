package run.halo.app.controller.content.model;

import static run.halo.app.model.support.HaloConst.POST_PASSWORD_TEMPLATE;
import static run.halo.app.model.support.HaloConst.SUFFIX_FTL;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import run.halo.app.cache.AbstractStringCacheStore;
import run.halo.app.controller.content.auth.PostAuthentication;
import run.halo.app.exception.ForbiddenException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.PostMeta;
import run.halo.app.model.entity.Tag;
import run.halo.app.model.enums.EncryptTypeEnum;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.vo.ArchiveYearVO;
import run.halo.app.model.vo.PostDetailVO;
import run.halo.app.model.vo.PostListVO;
import run.halo.app.service.CategoryService;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostCategoryService;
import run.halo.app.service.PostMetaService;
import run.halo.app.service.PostService;
import run.halo.app.service.PostTagService;
import run.halo.app.service.TagService;
import run.halo.app.service.ThemeService;
import run.halo.app.service.assembler.PostRenderAssembler;

/**
 * Post Model
 *
 * @author ryanwang
 * @author guqing
 * @date 2020-01-07
 */
@Component
public class PostModel {

    private final PostRenderAssembler postRenderAssembler;

    private final PostService postService;

    private final ThemeService themeService;

    private final PostCategoryService postCategoryService;

    private final CategoryService categoryService;

    private final PostTagService postTagService;

    private final TagService tagService;

    private final PostMetaService postMetaService;

    private final OptionService optionService;

    private final AbstractStringCacheStore cacheStore;

    private final PostAuthentication postAuthentication;

    public PostModel(PostRenderAssembler postRenderAssembler,
        PostService postService,
        ThemeService themeService,
        PostCategoryService postCategoryService,
        CategoryService categoryService,
        PostMetaService postMetaService,
        PostTagService postTagService,
        TagService tagService,
        OptionService optionService,
        AbstractStringCacheStore cacheStore,
        PostAuthentication postAuthentication) {
        this.postRenderAssembler = postRenderAssembler;
        this.postService = postService;
        this.themeService = themeService;
        this.postCategoryService = postCategoryService;
        this.categoryService = categoryService;
        this.postMetaService = postMetaService;
        this.postTagService = postTagService;
        this.tagService = tagService;
        this.optionService = optionService;
        this.cacheStore = cacheStore;
        this.postAuthentication = postAuthentication;
    }

    public String content(Post post, String token, Model model) {
        if (PostStatus.RECYCLE.equals(post.getStatus())) {
            // Articles in the recycle bin are not allowed to be accessed.
            throw new NotFoundException("查询不到该文章的信息");
        } else if (StringUtils.isNotBlank(token)) {
            // If the token is not empty, it means it is an admin request,
            // then verify the token.

            // verify token
            String cachedToken = cacheStore.getAny(token, String.class)
                .orElseThrow(() -> new ForbiddenException("您没有该文章的访问权限"));
            if (!cachedToken.equals(token)) {
                throw new ForbiddenException("您没有该文章的访问权限");
            }
        } else if (PostStatus.DRAFT.equals(post.getStatus())) {
            // Drafts are not allowed bo be accessed by outsiders.
            throw new NotFoundException("查询不到该文章的信息");
        } else if (PostStatus.INTIMATE.equals(post.getStatus())
            && !postAuthentication.isAuthenticated(post.getId())
        ) {
            // Encrypted articles must has the correct password before they can be accessed.

            model.addAttribute("slug", post.getSlug());
            model.addAttribute("type", EncryptTypeEnum.POST.getName());
            if (themeService.templateExists(POST_PASSWORD_TEMPLATE + SUFFIX_FTL)) {
                return themeService.render(POST_PASSWORD_TEMPLATE);
            }
            return "common/template/" + POST_PASSWORD_TEMPLATE;
        }

        post = postService.getById(post.getId());

        postService.publishVisitEvent(post.getId());

        postService.getPrevPost(post)
            .ifPresent(prevPost -> model.addAttribute("prevPost",
                postRenderAssembler.convertToDetailVo(prevPost)));
        postService.getNextPost(post)
            .ifPresent(nextPost -> model.addAttribute("nextPost",
                postRenderAssembler.convertToDetailVo(nextPost)));

        List<Category> categories = postCategoryService.listCategoriesBy(post.getId());
        List<Tag> tags = postTagService.listTagsBy(post.getId());
        List<PostMeta> metas = postMetaService.listBy(post.getId());

        // Generate meta keywords.
        if (StringUtils.isNotEmpty(post.getMetaKeywords())) {
            model.addAttribute("meta_keywords", post.getMetaKeywords());
        } else {
            model.addAttribute("meta_keywords",
                tags.stream().map(Tag::getName).collect(Collectors.joining(",")));
        }

        model.addAttribute("is_post", true);

        PostDetailVO postDetail;
        if (StringUtils.isNotBlank(token)) {
            postDetail = postRenderAssembler.convertToPreviewDetailVo(post);
            model.addAttribute("post", postDetail);
        } else {
            postDetail = postRenderAssembler.convertToDetailVo(post);
            model.addAttribute("post", postDetail);
        }

        // Generate meta description.
        if (StringUtils.isNotEmpty(post.getMetaDescription())) {
            model.addAttribute("meta_description", post.getMetaDescription());
        } else {
            model.addAttribute("meta_description",
                postService.generateDescription(postDetail.getContent()));
        }


        model.addAttribute("categories", categoryService.convertTo(categories));
        model.addAttribute("tags", tagService.convertTo(tags));
        model.addAttribute("metas", postMetaService.convertToMap(metas));

        if (themeService.templateExists(
            ThemeService.CUSTOM_POST_PREFIX + post.getTemplate() + SUFFIX_FTL)) {
            return themeService.render(ThemeService.CUSTOM_POST_PREFIX + post.getTemplate());
        }

        return themeService.render("post");
    }

    public String list(Integer page, Model model) {
        int pageSize = optionService.getPostPageSize();
        Pageable pageable = PageRequest
            .of(page >= 1 ? page - 1 : page, pageSize, postService.getPostDefaultSort());

        Page<Post> postPage = postService.pageBy(PostStatus.PUBLISHED, pageable);
        Page<PostListVO> posts = postRenderAssembler.convertToListVo(postPage);

        model.addAttribute("is_index", true);
        model.addAttribute("posts", posts);
        model.addAttribute("meta_keywords", optionService.getSeoKeywords());
        model.addAttribute("meta_description", optionService.getSeoDescription());
        return themeService.render("index");
    }

    public String archives(Integer page, Model model) {
        int pageSize = optionService.getArchivesPageSize();
        Pageable pageable = PageRequest
            .of(page >= 1 ? page - 1 : page, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));

        Page<Post> postPage = postService.pageBy(PostStatus.PUBLISHED, pageable);

        Page<PostListVO> posts = postRenderAssembler.convertToListVo(postPage);

        List<ArchiveYearVO> archives =
            postRenderAssembler.convertToYearArchives(postPage.getContent());

        model.addAttribute("is_archives", true);
        model.addAttribute("posts", posts);
        model.addAttribute("archives", archives);
        model.addAttribute("meta_keywords", optionService.getSeoKeywords());
        model.addAttribute("meta_description", optionService.getSeoDescription());
        return themeService.render("archives");
    }
}
