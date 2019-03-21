package cc.ryanc.halo.web.controller.admin.api;

import cc.ryanc.halo.model.dto.CategoryOutputDTO;
import cc.ryanc.halo.model.entity.Category;
import cc.ryanc.halo.model.params.CategoryParam;
import cc.ryanc.halo.model.vo.CategoryVO;
import cc.ryanc.halo.service.CategoryService;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;

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

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("tree")
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
}
