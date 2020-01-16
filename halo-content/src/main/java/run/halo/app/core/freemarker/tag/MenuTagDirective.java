package run.halo.app.core.freemarker.tag;

import freemarker.core.Environment;
import freemarker.template.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import run.halo.app.model.support.HaloConst;
import run.halo.app.service.MenuService;

import java.io.IOException;
import java.util.Map;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Freemarker custom tag of menu.
 *
 * @author ryanwang
 * @date 2019-03-22
 */
@Component
public class MenuTagDirective implements TemplateDirectiveModel {

    private static final String METHOD_KEY = "method";

    private final MenuService menuService;

    public MenuTagDirective(Configuration configuration, MenuService menuService) {
        this.menuService = menuService;
        configuration.setSharedVariable("menuTag", this);
    }

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        final DefaultObjectWrapperBuilder builder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25);

        if (params.containsKey(HaloConst.METHOD_KEY)) {
            String method = params.get(HaloConst.METHOD_KEY).toString();
            switch (method) {
                case "list":
                    env.setVariable("menus", builder.build().wrap(menuService.listAll()));
                    break;
                case "tree":
                    env.setVariable("menus", builder.build().wrap(menuService.listAsTree(Sort.by(DESC, "priority"))));
                    break;
                case "listTeams":
                    env.setVariable("teams", builder.build().wrap(menuService.listTeamVos(Sort.by(DESC, "priority"))));
                    break;
                case "listByTeam":
                    String team = params.get("team").toString();
                    env.setVariable("menus", builder.build().wrap(menuService.listByTeam(team, Sort.by(DESC, "priority"))));
                    break;
                case "treeByTeam":
                    String treeTeam = params.get("team").toString();
                    env.setVariable("menus", builder.build().wrap(menuService.listByTeamAsTree(treeTeam, Sort.by(DESC, "priority"))));
                    break;
                case "count":
                    env.setVariable("count", builder.build().wrap(menuService.count()));
                    break;
                default:
                    break;
            }
        }
        body.render(env.getOut());
    }
}
