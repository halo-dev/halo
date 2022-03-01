package run.halo.app.controller.admin.api;

import static org.springframework.data.domain.Sort.Direction.ASC;

import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.model.dto.CategoryDTO;
import run.halo.app.model.entity.Category;
import run.halo.app.model.params.CategoryParam;
import run.halo.app.model.vo.CategoryVO;
import run.halo.app.service.CategoryService;
import run.halo.app.service.PostCategoryService;

/**
 * Category controller.
 *
 * @author johnniang
 * @date 2019-03-21
 */
@RestController
@RequestMapping("/api/admin/categories")
public class CategoryController {

    private final CategoryService categoryService;

    private final PostCategoryService postCategoryService;

    public CategoryController(CategoryService categoryService,
        PostCategoryService postCategoryService) {
        this.categoryService = categoryService;
        this.postCategoryService = postCategoryService;
    }

    @GetMapping("{categoryId:\\d+}")
    @ApiOperation("Gets category detail")
    public CategoryDTO getBy(@PathVariable("categoryId") Integer categoryId) {
        return categoryService.convertTo(categoryService.getById(categoryId));
    }

    @GetMapping
    @ApiOperation("Lists all categories")
    public List<? extends CategoryDTO> listAll(
        @SortDefault(sort = "priority", direction = ASC) Sort sort,
        @RequestParam(name = "more", required = false, defaultValue = "false") boolean more) {
        if (more) {
            return postCategoryService.listCategoryWithPostCountDto(sort);
        }

        return categoryService.convertTo(categoryService.listAll(sort));
    }

    @GetMapping("tree_view")
    @ApiOperation("List all categories as tree")
    public List<CategoryVO> listAsTree(
        @SortDefault(sort = "priority", direction = ASC) Sort sort) {
        return categoryService.listAsTree(sort);
    }

    @PostMapping
    @ApiOperation("Creates category")
    public CategoryDTO createBy(@RequestBody @Valid CategoryParam categoryParam) {
        // Convert to category
        Category category = categoryParam.convertTo();

        // Save it
        return categoryService.convertTo(categoryService.create(category));
    }

    @PutMapping("{categoryId:\\d+}")
    @ApiOperation("Updates category")
    public CategoryDTO updateBy(@PathVariable("categoryId") Integer categoryId,
        @RequestBody @Valid CategoryParam categoryParam
    ) {
        Category categoryToUpdate = categoryService.getById(categoryId);
        categoryParam.update(categoryToUpdate);
        return categoryService.convertTo(categoryService.update(categoryToUpdate));
    }

    @PutMapping("/batch")
    @ApiOperation("Updates category in batch")
    public List<CategoryDTO> updateBatchBy(@RequestBody List<@Valid CategoryParam> categoryParams) {
        List<Category> categoriesToUpdate = categoryParams.stream()
            .filter(categoryParam -> Objects.nonNull(categoryParam.getId()))
            .map(categoryParam -> {
                Category categoryToUpdate = categoryService.getById(categoryParam.getId());
                categoryParam.update(categoryToUpdate);
                return categoryToUpdate;
            })
            .collect(Collectors.toList());
        return categoryService.convertTo(categoryService.updateInBatch(categoriesToUpdate));
    }

    @DeleteMapping("{categoryId:\\d+}")
    @ApiOperation("Deletes category")
    public void deletePermanently(@PathVariable("categoryId") Integer categoryId) {
        categoryService.removeCategoryAndPostCategoryBy(categoryId);
    }
}
