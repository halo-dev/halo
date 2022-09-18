package run.halo.app.theme.dialect;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;

/**
 * <p>The <code>head</code> html snippet injection processor for post template.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PostTemplateHeadProcessor implements TemplateHeadProcessor {
    private static final String POST_NAME_VARIABLE = "name";
    private final PostFinder postFinder;


    public PostTemplateHeadProcessor(PostFinder postFinder) {
        this.postFinder = postFinder;
    }

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model,
        IElementModelStructureHandler structureHandler) {
        return Mono.just(context.getTemplateData().getTemplate())
            .filter(this::isPostTemplate)
            .map(template -> (String) context.getVariable(POST_NAME_VARIABLE))
            .map(postFinder::getByName)
            .doOnNext(postVo -> {
                List<Map<String, String>> htmlMetas = postVo.getSpec().getHtmlMetas();
                String metaHtml = headMetaBuilder(htmlMetas);
                IModelFactory modelFactory = context.getModelFactory();
                model.add(modelFactory.createText(metaHtml));
            })
            .then();
    }

    private String headMetaBuilder(List<Map<String, String>> htmlMetas) {
        if (htmlMetas == null) {
            return StringUtils.EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        for (Map<String, String> htmlMeta : htmlMetas) {
            sb.append("<meta");
            htmlMeta.forEach((k, v) -> {
                sb.append(" ").append(k).append("=\"").append(v).append("\"");
            });
            sb.append(" />\n");
        }
        return sb.toString();
    }

    private boolean isPostTemplate(String template) {
        return DefaultTemplateEnum.POST.getValue().equals(template);
    }
}