package run.halo.app.model.freemarker.tag;

import freemarker.core.Environment;
import freemarker.template.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import run.halo.app.model.support.HaloConst;
import run.halo.app.service.PostTagService;
import run.halo.app.service.TagService;

import java.io.IOException;
import java.util.Map;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Freemarker custom tag of tag.
 *
 * @author ryanwang
 * @date : 2019/3/22
 */
@Component
public class TagTagDirective implements TemplateDirectiveModel {

    private final TagService tagService;

    private final PostTagService postTagService;

    public TagTagDirective(Configuration configuration, TagService tagService, PostTagService postTagService) {
        this.tagService = tagService;
        this.postTagService = postTagService;
        configuration.setSharedVariable("tagTag", this);
    }

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        final DefaultObjectWrapperBuilder builder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25);

        if (params.containsKey(HaloConst.METHOD_KEY)) {
            String method = params.get(HaloConst.METHOD_KEY).toString();
            switch (method) {
                case "list":
                    env.setVariable("tags", builder.build().wrap(postTagService.listTagWithCountDtos(Sort.by(DESC, "createTime"))));
                    break;
                case "listByPostId":
                    Integer postId = Integer.parseInt(params.get("postId").toString());
                    env.setVariable("tags", builder.build().wrap(postTagService.listTagsBy(postId)));
                    break;
                case "count":
                    env.setVariable("count", builder.build().wrap(tagService.count()));
                    break;
                default:
                    break;
            }
        }
        body.render(env.getOut());
    }
}
