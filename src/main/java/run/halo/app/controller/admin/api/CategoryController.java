package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.dto.CategoryDTO;
import run.halo.app.model.entity.Category;
import run.halo.app.model.params.CategoryParam;
import run.halo.app.model.vo.CategoryVO;
import run.halo.app.service.CategoryService;
import run.halo.app.service.PostCategoryService;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Category controller.
 *
 * @author johnniang
 * @date 3/21/19
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


    /**
     * Get Category by id
     *
     * @param id id
     * @return CategoryDTO
     */
    @GetMapping("{id:\\d+}")
    @ApiOperation("Get category detail by id")
    public CategoryDTO getBy(@PathVariable("id") Integer id) {
        return new CategoryDTO().convertFrom(categoryService.getById(id));
    }

    @GetMapping
    @ApiOperation("List all categories")
    public List<? extends CategoryDTO> listAll(
            @SortDefault(sort = "updateTime", direction = DESC) Sort sort,
            @RequestParam(name = "more", required = false, defaultValue = "false") boolean more) {
        if (more) {
            return postCategoryService.listCategoryWithPostCountDto(sort);
        }

        return categoryService.convertTo(categoryService.listAll(sort));
    }

    @GetMapping("tree_view")
    @ApiOperation("List as category tree")
    public List<CategoryVO> listAsTree(@SortDefault(sort = "name", direction = ASC) Sort sort) {
        return categoryService.listAsTree(sort);
    }

    @PostMapping
    public CategoryDTO createBy(@Valid @RequestBody CategoryParam categoryParam) {
        // Convert to category
        Category category = categoryParam.convertTo();

        // Save it
        return new CategoryDTO().convertFrom(categoryService.create(category));
    }

    @PutMapping("{id:\\d+}")
    @ApiOperation("Update category")
    public CategoryDTO updateBy(@PathVariable("id") Integer id,
                                @RequestBody @Valid CategoryParam categoryParam) {
        Category categoryToUpdate = categoryService.getById(id);
        categoryParam.update(categoryToUpdate);
        return new CategoryDTO().convertFrom(categoryService.update(categoryToUpdate));
    }

    /**
     * Delete category by id.
     *
     * @param id id
     */
    @DeleteMapping("{id:\\d+}")
    @ApiOperation("Delete category")
    public void deletePermanently(@PathVariable("id") Integer id) {
        categoryService.removeCategoryAndPostCategoryBy(id);
    }
}
