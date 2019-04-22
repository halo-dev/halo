package run.halo.app.model.freemarker.tag;

import freemarker.core.Environment;
import freemarker.template.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import run.halo.app.model.support.HaloConst;
import run.halo.app.service.GalleryService;

import java.io.IOException;
import java.util.Map;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Freemarker custom tag of gallery.
 *
 * @author : RYAN0UP
 * @date : 2019/4/21
 */
@Component
public class GalleryTagDirective implements TemplateDirectiveModel {

    private final GalleryService galleryService;

    public GalleryTagDirective(GalleryService galleryService) {
        this.galleryService = galleryService;
    }

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        final DefaultObjectWrapperBuilder builder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25);

        if (params.containsKey(HaloConst.METHOD_KEY)) {
            String method = params.get(HaloConst.METHOD_KEY).toString();
            String team = params.get("team").toString();
            switch (method) {
                case "list":
                    env.setVariable("galleries", builder.build().wrap(galleryService.listAll()));
                    break;
                case "listTeams":
                    env.setVariable("teams", builder.build().wrap(galleryService.listDtos(Sort.by(DESC, "createTime"))));
                    break;
                case "listByTeam":
                    env.setVariable("galleries", builder.build().wrap(galleryService.listByTeam(team, Sort.by(DESC, "createTime"))));
                    break;
                case "count":
                    env.setVariable("count", builder.build().wrap(galleryService.count()));
                    break;
                default:
                    break;
            }
        }
        body.render(env.getOut());
    }
}
