package run.halo.app.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import run.halo.app.model.entity.Category;
import run.halo.app.model.vo.CategoryVO;
import run.halo.app.repository.CategoryRepository;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostCategoryService;
import run.halo.app.utils.JsonUtils;

/**
 * Category service test.
 *
 * @author guqing
 * @date 2021-12-04
 */
@ExtendWith(SpringExtension.class)
public class CategoryServiceTest {

    @Autowired
    private ApplicationContext applicationContext;

    @MockBean
    private CategoryRepository categoryRepository;

    @MockBean
    private PostCategoryService postCategoryService;

    @MockBean
    private OptionService optionService;

    private CategoryServiceImpl categoryService;

    @BeforeEach
    public void setUp() {
        categoryService =
            new CategoryServiceImpl(categoryRepository, postCategoryService, optionService,
                applicationContext);

        when(optionService.isEnabledAbsolutePath()).thenReturn(true);
        when(optionService.getBlogBaseUrl()).thenReturn("http://127.0.0.1:8090");
        when(optionService.getCategoriesPrefix()).thenReturn("categories");
        when(optionService.getPathSuffix()).thenReturn("");
    }

    @Test
    void listToTree() throws JsonProcessingException {
        List<Category> categories = mockCategories();
        List<CategoryVO> categoryVoList = categoryService.listToTree(categories);
        assertEquals(
            "[{\"id\":1,\"name\":\"分类-1\",\"slug\":\"category1\",\"description\":null,"
                + "\"thumbnail\":null,\"parentId\":0,\"password\":null,\"createTime\":null,"
                + "\"fullPath\":\"http://127.0.0.1:8090/categories/category1\",\"priority\":0,"
                + "\"children\":[]},{\"id\":2,\"name\":\"分类-2\",\"slug\":\"category2\","
                + "\"description\":null,\"thumbnail\":null,\"parentId\":0,\"password\":null,"
                + "\"createTime\":null,\"fullPath\":\"http://127.0.0.1:8090/categories/category2\","
                + "\"priority\":1,\"children\":[{\"id\":3,\"name\":\"分类-2-1\","
                + "\"slug\":\"category21\",\"description\":null,\"thumbnail\":null,\"parentId\":2,"
                + "\"password\":null,\"createTime\":null,\"fullPath\":\"http://127.0.0"
                + ".1:8090/categories/category21\",\"priority\":2,\"children\":[{\"id\":5,"
                + "\"name\":\"分类-2-1-1\",\"slug\":\"category211\",\"description\":null,"
                + "\"thumbnail\":null,\"parentId\":3,\"password\":null,\"createTime\":null,"
                + "\"fullPath\":\"http://127.0.0.1:8090/categories/category211\",\"priority\":4,"
                + "\"children\":[{\"id\":6,\"name\":\"分类-2-1-1-1\",\"slug\":\"category2111\","
                + "\"description\":null,\"thumbnail\":null,\"parentId\":5,\"password\":null,"
                + "\"createTime\":null,\"fullPath\":\"http://127.0.0"
                + ".1:8090/categories/category2111\",\"priority\":5,\"children\":[]}]}]},"
                + "{\"id\":4,\"name\":\"分类-2-2\",\"slug\":\"category22\",\"description\":null,"
                + "\"thumbnail\":null,\"parentId\":2,\"password\":null,\"createTime\":null,"
                + "\"fullPath\":\"http://127.0.0.1:8090/categories/category22\",\"priority\":3,"
                + "\"children\":[]}]}]",
            JsonUtils.objectToJson(categoryVoList));
    }

    private List<Category> mockCategories() {
        Category category1 = new Category();
        category1.setId(1);
        category1.setName("分类-1");
        category1.setSlug("category1");
        category1.setParentId(0);
        category1.setPriority(0);

        Category category2 = new Category();
        category2.setId(2);
        category2.setName("分类-2");
        category2.setSlug("category2");
        category2.setParentId(0);
        category2.setPriority(1);

        Category category3 = new Category();
        category3.setId(3);
        category3.setName("分类-2-1");
        category3.setSlug("category21");
        category3.setParentId(2);
        category3.setPriority(2);

        Category category4 = new Category();
        category4.setId(4);
        category4.setName("分类-2-2");
        category4.setSlug("category22");
        category4.setParentId(2);
        category4.setPriority(3);

        Category category5 = new Category();
        category5.setId(5);
        category5.setName("分类-2-1-1");
        category5.setSlug("category211");
        category5.setParentId(3);
        category5.setPriority(4);

        Category category6 = new Category();
        category6.setId(6);
        category6.setName("分类-2-1-1-1");
        category6.setSlug("category2111");
        category6.setParentId(5);
        category6.setPriority(5);
        return List.of(category1, category2, category3, category4, category5, category6);
    }
}
