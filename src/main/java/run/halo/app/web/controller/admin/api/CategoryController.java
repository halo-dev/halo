package run.halo.app.web.controller.admin.api;

import run.halo.app.model.dto.CategoryOutputDTO;
import run.halo.app.model.entity.Category;
import run.halo.app.model.params.CategoryParam;
import run.halo.app.model.vo.CategoryVO;
import run.halo.app.service.CategoryService;
import run.halo.app.service.PostCategoryService;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.dto.CategoryOutputDTO;
import run.halo.app.model.entity.Category;
import run.halo.app.model.params.CategoryParam;
import run.halo.app.model.vo.CategoryVO;
import run.halo.app.service.CategoryService;
import run.halo.app.service.PostCategoryService;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.ASC;

/**
 * Category controller.
 *
 * @author johnniang
 * @date 3/21/19
 */
@RestController
@RequestMapping("/admin/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    private final PostCategoryService postCategoryService;

    public CategoryController(CategoryService categoryService, PostCategoryService postCategoryService) {
        this.categoryService = categoryService;
        this.postCategoryService = postCategoryService;
    }

    @GetMapping("tree_view")
    @ApiOperation("List as category tree")
    public List<CategoryVO> listAsTree(@SortDefault(sort = "name", direction = ASC) Sort sort) {
        return categoryService.listAsTree(sort);
    }

    @PostMapping
    public CategoryOutputDTO createBy(@Valid @RequestBody CategoryParam categoryParam) {
        // Convert to category
        Category category = categoryParam.convertTo();

        // Save it
        return new CategoryOutputDTO().convertFrom(categoryService.create(category));
    }

    /**
     * Get Category by id
     *
     * @param id id
     * @return CategoryOutputDTO
     */
    @GetMapping("{id:\\d+}")
    @ApiOperation("Get category detail by id")
    public CategoryOutputDTO getBy(@PathVariable("id") Integer id) {
        return new CategoryOutputDTO().convertFrom(categoryService.getById(id));
    }

    /**
     * Delete category by id.
     *
     * @param id id
     */
    @DeleteMapping("{id:\\d+}")
    @ApiOperation("Delete category by id")
    public void deletePermanently(@PathVariable("id") Integer id) {
        categoryService.removeById(id);
        postCategoryService.removeByCategoryId(id);
    }
}
