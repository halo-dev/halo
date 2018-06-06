package cc.ryanc.halo.model.tag;

import cc.ryanc.halo.service.*;
import freemarker.core.Environment;
import freemarker.template.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * <pre>
 *     FreeMarker自定义标签
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/4/26
 */
@Component
public class CommonTagDirective implements TemplateDirectiveModel {

    private static final String METHOD_KEY = "method";

    @Autowired
    private MenuService menuService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private TagService tagService;

    @Autowired
    private LinkService linkService;

    @Override
    public void execute(Environment environment, Map map, TemplateModel[] templateModels, TemplateDirectiveBody templateDirectiveBody) throws TemplateException, IOException {
        DefaultObjectWrapperBuilder builder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25);
        if (map.containsKey(METHOD_KEY)) {
            String method = map.get(METHOD_KEY).toString();
            switch (method) {
                case "menus":
                    environment.setVariable("menus", builder.build().wrap(menuService.findAllMenus()));
                    break;
                case "categories":
                    environment.setVariable("categories", builder.build().wrap(categoryService.findAllCategories()));
                    break;
                case "tags":
                    environment.setVariable("tags", builder.build().wrap(tagService.findAllTags()));
                    break;
                case "links":
                    environment.setVariable("links", builder.build().wrap(linkService.findAllLinks()));
                    break;
                case "newComments":
                    environment.setVariable("newComments", builder.build().wrap(commentService.findAllComments(1)));
                    break;
                default:
                    break;
            }
        }
        templateDirectiveBody.render(environment.getOut());
    }
}
