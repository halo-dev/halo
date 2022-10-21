package run.halo.app.core.freemarker.tag;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import java.io.IOException;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import run.halo.app.model.entity.PostComment;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.model.support.HaloConst;
import run.halo.app.service.PostCommentService;
import run.halo.app.service.assembler.comment.PostCommentRenderAssembler;

/**
 * Freemarker custom tag of comment.
 *
 * @author ryanwang
 * @author guqing
 * @date 2019-03-22
 */
@Component
public class CommentTagDirective implements TemplateDirectiveModel {

    private final PostCommentService postCommentService;

    private final PostCommentRenderAssembler postCommentRenderAssembler;

    public CommentTagDirective(Configuration configuration, PostCommentService postCommentService,
        PostCommentRenderAssembler postCommentRenderAssembler) {
        this.postCommentService = postCommentService;
        this.postCommentRenderAssembler = postCommentRenderAssembler;
        configuration.setSharedVariable("commentTag", this);
    }

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars,
        TemplateDirectiveBody body) throws TemplateException, IOException {
        final DefaultObjectWrapperBuilder builder =
            new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25);

        if (params.containsKey(HaloConst.METHOD_KEY)) {
            String method = params.get(HaloConst.METHOD_KEY).toString();
            switch (method) {
                case "latest":
                    int top = Integer.parseInt(params.get("top").toString());
                    Page<PostComment> postComments =
                        postCommentService.pageLatest(top, CommentStatus.PUBLISHED);
                    env.setVariable("comments",
                        builder.build()
                            .wrap(postCommentRenderAssembler.convertToWithPostVo(postComments)));
                    break;
                case "count":
                    env.setVariable("count", builder.build().wrap(postCommentService.count()));
                    break;
                default:
                    break;
            }
        }
        body.render(env.getOut());
    }
}
