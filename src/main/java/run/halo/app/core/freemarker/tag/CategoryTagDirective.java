package run.halo.app.core.freemarker.tag;

import static org.springframework.data.domain.Sort.Direction.DESC;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import run.halo.app.model.entity.Category;
import run.halo.app.model.support.HaloConst;
import run.halo.app.service.CategoryService;
import run.halo.app.service.PostCategoryService;

/**
 * Freemarker custom tag of category.
 *
 * @author ryanwang
 * @date 2019-03-22
 */
@Component
public class CategoryTagDirective implements TemplateDirectiveModel {

    private final CategoryService categoryService;

    private final PostCategoryService postCategoryService;

    public CategoryTagDirective(Configuration configuration,
        CategoryService categoryService,
        PostCategoryService postCategoryService) {
        this.categoryService = categoryService;
        this.postCategoryService = postCategoryService;
        configuration.setSharedVariable("categoryTag", this);
    }

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars,
        TemplateDirectiveBody body) throws TemplateException, IOException {
        final DefaultObjectWrapperBuilder builder =
            new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25);

        if (params.containsKey(HaloConst.METHOD_KEY)) {
            String method = params.get(HaloConst.METHOD_KEY).toString();
            switch (method) {
                case "list":
                    env.setVariable("categories", builder.build().wrap(postCategoryService
                        .listCategoryWithPostCountDto(Sort.by(DESC, "createTime"), false)));
                    break;
                case "tree":
                    env.setVariable("categories", builder.build()
                        .wrap(categoryService.listAsTree(Sort.by(DESC, "createTime"))));
                    break;
                case "listByPostId":
                    Integer postId = Integer.parseInt(params.get("postId").toString());
                    List<Category> categories = postCategoryService.listCategoriesBy(postId);
                    env.setVariable("categories",
                        builder.build().wrap(categoryService.convertTo(categories)));
                    break;
                case "count":
                    env.setVariable("count", builder.build().wrap(categoryService.count()));
                    break;
                default:
                    break;
            }
        }
        body.render(env.getOut());
    }
}
