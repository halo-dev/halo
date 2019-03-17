package cc.ryanc.halo.model.freemarker.tag;

import cc.ryanc.halo.service.*;
import freemarker.core.Environment;
import freemarker.template.*;
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

    private final MenuService menuService;

    private final CategoryService categoryService;

    private final TagService tagService;

    private final LinkService linkService;

    private final CommentService commentService;

    public CommonTagDirective(MenuService menuService,
                              CategoryService categoryService,
                              TagService tagService,
                              LinkService linkService,
                              CommentService commentService) {
        this.menuService = menuService;
        this.categoryService = categoryService;
        this.tagService = tagService;
        this.linkService = linkService;
        this.commentService = commentService;
    }

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        final DefaultObjectWrapperBuilder builder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25);

        if (params.containsKey(METHOD_KEY)) {
            String method = params.get(METHOD_KEY).toString();
            switch (method) {
                case "menus":
                    env.setVariable("menus", builder.build().wrap(menuService.listAll()));
                    break;
                case "categories":
                    env.setVariable("categories", builder.build().wrap(categoryService.listAll()));
                    break;
                case "tags":
                    env.setVariable("tags", builder.build().wrap(tagService.listAll()));
                    break;
                case "links":
                    env.setVariable("links", builder.build().wrap(linkService.listAll()));
                    break;
                case "newComments":
                    env.setVariable("newComments", builder.build().wrap(commentService.listAll()));
                    break;
                default:
                    break;
            }
        }
        body.render(env.getOut());
    }

}
