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
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import run.halo.app.model.support.HaloConst;
import run.halo.app.service.PhotoService;

/**
 * Freemarker custom tag of photo.
 *
 * @author ryanwang
 * @date 2019-04-21
 */
@Component
public class PhotoTagDirective implements TemplateDirectiveModel {

    private final PhotoService photoService;

    public PhotoTagDirective(Configuration configuration, PhotoService photoService) {
        this.photoService = photoService;
        configuration.setSharedVariable("photoTag", this);
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
                    env.setVariable("photos", builder.build().wrap(photoService.listAll()));
                    break;
                case "listTeams":
                    env.setVariable("teams", builder.build()
                        .wrap(photoService.listTeamVos(Sort.by(DESC, "createTime"))));
                    break;
                case "listByTeam":
                    String team = params.get("team").toString();
                    env.setVariable("photos", builder.build()
                        .wrap(photoService.listByTeam(team, Sort.by(DESC, "createTime"))));
                    break;
                case "count":
                    env.setVariable("count", builder.build().wrap(photoService.count()));
                    break;
                default:
                    break;
            }
        }
        body.render(env.getOut());
    }
}
