package cc.ryanc.halo.model.freemarker.tag;

import cc.ryanc.halo.service.MenuService;
import freemarker.core.Environment;
import freemarker.template.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Freemarker custom tag of menu.
 *
 * @author : RYAN0UP
 * @date : 2019/3/22
 */
@Component
public class MenuTagDirective implements TemplateDirectiveModel {

    private static final String METHOD_KEY = "method";

    private final MenuService menuService;

    public MenuTagDirective(MenuService menuService) {
        this.menuService = menuService;
    }

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        final DefaultObjectWrapperBuilder builder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25);

        if (params.containsKey(METHOD_KEY)) {
            String method = params.get(METHOD_KEY).toString();
            switch (method) {
                case "list":
                    env.setVariable("menus", builder.build().wrap(menuService.listAll()));
                    break;
                case "count":
                    env.setVariable("count", builder.build().wrap(menuService.count()));
                default:
                    break;
            }
        }
        body.render(env.getOut());
    }
}
