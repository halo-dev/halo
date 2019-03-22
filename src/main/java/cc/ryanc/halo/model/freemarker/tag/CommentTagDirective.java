package cc.ryanc.halo.model.freemarker.tag;

import cc.ryanc.halo.service.CommentService;
import freemarker.core.Environment;
import freemarker.template.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * @author : RYAN0UP
 * @date : 2019/3/22
 */
@Component
public class CommentTagDirective implements TemplateDirectiveModel {

    private static final String METHOD_KEY = "method";

    private final CommentService commentService;

    public CommentTagDirective(CommentService commentService) {
        this.commentService = commentService;
    }

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        final DefaultObjectWrapperBuilder builder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25);

        if (params.containsKey(METHOD_KEY)) {
            String method = params.get(METHOD_KEY).toString();
            int top = Integer.parseInt(params.get("top").toString());
            switch (method) {
                case "latest":
                    env.setVariable("categories", builder.build().wrap(builder.build().wrap(builder.build().wrap(commentService.pageLatest(top)))));
                    break;
                default:
                    break;
            }
        }
        body.render(env.getOut());
    }
}
