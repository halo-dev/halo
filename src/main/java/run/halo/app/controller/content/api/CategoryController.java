package run.halo.app.controller.content.api;

import static org.springframework.data.domain.Sort.Direction.DESC;

import com.google.common.collect.Sets;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.controller.content.auth.CategoryAuthentication;
import run.halo.app.controller.content.auth.ContentAuthenticationManager;
import run.halo.app.controller.content.auth.ContentAuthenticationRequest;
import run.halo.app.exception.ForbiddenException;
import run.halo.app.model.dto.CategoryDTO;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Post;
import run.halo.app.model.enums.EncryptTypeEnum;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.vo.PostListVO;
import run.halo.app.service.CategoryService;
import run.halo.app.service.PostCategoryService;
import run.halo.app.service.assembler.PostRenderAssembler;

/**
 * Content category controller.
 *
 * @author ryanwang
 * @date 2019-06-09
 */
@RestController("ApiContentCategoryController")
@RequestMapping("/api/content/categories")
public class CategoryController {

    private final CategoryService categoryService;

    private final PostCategoryService postCategoryService;

    private final PostRenderAssembler postRenderAssembler;

    private final CategoryAuthentication categoryAuthentication;

    private final ContentAuthenticationManager contentAuthenticationManager;

    public CategoryController(CategoryService categoryService,
        PostCategoryService postCategoryService,
        PostRenderAssembler postRenderAssembler,
        CategoryAuthentication categoryAuthentication,
        ContentAuthenticationManager contentAuthenticationManager) {
        this.categoryService = categoryService;
        this.postCategoryService = postCategoryService;
        this.postRenderAssembler = postRenderAssembler;
        this.categoryAuthentication = categoryAuthentication;
        this.contentAuthenticationManager = contentAuthenticationManager;
    }

    @GetMapping
    @ApiOperation("Lists categories")
    public List<? extends CategoryDTO> listCategories(
        @SortDefault(sort = "updateTime", direction = DESC) Sort sort,
        @RequestParam(name = "more", required = false, defaultValue = "false") Boolean more) {
        if (more) {
            return postCategoryService.listCategoryWithPostCountDto(sort);
        }
        return categoryService.convertTo(categoryService.listAll(sort));
    }

    @GetMapping("{slug}/posts")
    @ApiOperation("Lists posts by category slug")
    public Page<PostListVO> listPostsBy(@PathVariable("slug") String slug,
        @RequestParam(value = "password", required = false) String password,
        @PageableDefault(sort = {"topPriority", "updateTime"}, direction = DESC)
            Pageable pageable) {
        // Get category by slug
        Category category = categoryService.getBySlugOfNonNull(slug);

        Set<PostStatus> statusesToQuery = Sets.immutableEnumSet(PostStatus.PUBLISHED);
        if (allowIntimatePosts(category.getId(), password)) {
            statusesToQuery = Sets.immutableEnumSet(PostStatus.PUBLISHED, PostStatus.INTIMATE);
        }

        Page<Post> postPage =
            postCategoryService.pagePostBy(category.getId(), statusesToQuery, pageable);
        return postRenderAssembler.convertToListVo(postPage);
    }

    private boolean allowIntimatePosts(Integer categoryId, String password) {
        Assert.notNull(categoryId, "The categoryId must not be null.");

        if (!categoryService.isPrivate(categoryId)) {
            return false;
        }

        if (categoryAuthentication.isAuthenticated(categoryId)) {
            return true;
        }

        if (password != null) {
            ContentAuthenticationRequest authRequest =
                ContentAuthenticationRequest.of(categoryId, password,
                    EncryptTypeEnum.CATEGORY.getName());
            // authenticate this request,throw an error if authenticate failed
            contentAuthenticationManager.authenticate(authRequest);
            return true;
        }
        throw new ForbiddenException("您没有该分类的访问权限");
    }
}
