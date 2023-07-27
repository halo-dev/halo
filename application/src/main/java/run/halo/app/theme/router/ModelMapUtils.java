package run.halo.app.theme.router;

import java.util.HashMap;
import java.util.Map;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.Scheme;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.dialect.CommentWidget;
import run.halo.app.theme.finders.vo.PostVo;
import run.halo.app.theme.finders.vo.SinglePageVo;

/**
 * A util class for building model map.
 *
 * @author guqing
 * @since 2.6.0
 */
public abstract class ModelMapUtils {
    private static final Scheme POST_SCHEME = Scheme.buildFromType(Post.class);
    private static final Scheme SINGLE_PAGE_SCHEME = Scheme.buildFromType(SinglePage.class);

    /**
     * Build post view model.
     *
     * @param postVo post vo
     * @return model map
     */
    public static Map<String, Object> postModel(PostVo postVo) {
        Map<String, Object> model = new HashMap<>();
        model.put("name", postVo.getMetadata().getName());
        model.put(ModelConst.TEMPLATE_ID, DefaultTemplateEnum.POST.getValue());
        model.put("groupVersionKind", POST_SCHEME.groupVersionKind());
        model.put("plural", POST_SCHEME.plural());
        model.put("post", postVo);
        model.put(CommentWidget.ENABLE_COMMENT_ATTRIBUTE, postVo.getSpec().getAllowComment());
        return model;
    }

    /**
     * Build single page view model.
     *
     * @param pageVo page vo
     * @return model map
     */
    public static Map<String, Object> singlePageModel(SinglePageVo pageVo) {
        Map<String, Object> model = new HashMap<>();
        model.put("name", pageVo.getMetadata().getName());
        model.put("groupVersionKind", SINGLE_PAGE_SCHEME.groupVersionKind());
        model.put("plural", SINGLE_PAGE_SCHEME.plural());
        model.put(ModelConst.TEMPLATE_ID, DefaultTemplateEnum.SINGLE_PAGE.getValue());
        model.put("singlePage", pageVo);
        model.put(CommentWidget.ENABLE_COMMENT_ATTRIBUTE, pageVo.getSpec().getAllowComment());
        return model;
    }
}
