package run.halo.app.model.freemarker.tag;

import freemarker.core.Environment;
import freemarker.template.*;
import org.springframework.stereotype.Component;
import run.halo.app.model.support.HaloConst;
import run.halo.app.service.PostService;

import java.io.IOException;
import java.util.Map;

/**
 * Freemarker custom tag of post.
 *
 * @author ryanwang
 * @date : 2018/4/26
 */
@Component
public class PostTagDirective implements TemplateDirectiveModel {

    private final PostService postService;

    public PostTagDirective(PostService postService) {
        this.postService = postService;
    }

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        final DefaultObjectWrapperBuilder builder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25);
        if (params.containsKey(HaloConst.METHOD_KEY)) {
            String method = params.get(HaloConst.METHOD_KEY).toString();
            switch (method) {
                case "count":
                    env.setVariable("count", builder.build().wrap(postService.count()));
                    break;
                case "archiveYear":
                    env.setVariable("archives",builder.build().wrap(postService.listYearArchives()));
                    break;
                case "archiveMonth":
                    env.setVariable("archives",builder.build().wrap(postService.listMonthArchives()));
                    break;
                default:
                    break;
            }
        }
        body.render(env.getOut());
    }

}
