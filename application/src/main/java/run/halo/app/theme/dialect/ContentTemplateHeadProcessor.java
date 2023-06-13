package run.halo.app.theme.dialect;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.ITemplateEvent;
import org.thymeleaf.model.IText;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.SinglePageFinder;
import run.halo.app.theme.router.ModelConst;

/**
 * <p>The <code>head</code> html snippet injection processor for content template such as post
 * and page.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@Order(1)
@AllArgsConstructor
public class ContentTemplateHeadProcessor implements TemplateHeadProcessor {
    private static final String POST_NAME_VARIABLE = "name";
    static final Pattern META_PATTERN = Pattern.compile("<meta\\s+name=\"(\\w+)\"\\s+");
    private final PostFinder postFinder;
    private final SinglePageFinder singlePageFinder;

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model,
        IElementModelStructureHandler structureHandler) {
        Mono<String> nameMono = Mono.justOrEmpty((String) context.getVariable(POST_NAME_VARIABLE));

        Mono<List<Map<String, String>>> htmlMetasMono = Mono.empty();
        if (isPostTemplate(context)) {
            htmlMetasMono = nameMono.flatMap(postFinder::getByName)
                .map(post -> {
                    List<Map<String, String>> htmlMetas = post.getSpec().getHtmlMetas();
                    String excerpt =
                        post.getStatus() == null ? null : post.getStatus().getExcerpt();
                    return excerptToMetaDescriptionIfAbsent(htmlMetas, excerpt);
                });
        } else if (isPageTemplate(context)) {
            htmlMetasMono = nameMono.flatMap(singlePageFinder::getByName)
                .map(page -> {
                    List<Map<String, String>> htmlMetas = page.getSpec().getHtmlMetas();
                    String excerpt =
                        page.getStatus() == null ? null : page.getStatus().getExcerpt();
                    return excerptToMetaDescriptionIfAbsent(htmlMetas, excerpt);
                });
        }

        return htmlMetasMono
            .doOnNext(htmlMetas -> {
                Multimap<String, Integer> existingMetaPosMap = lookupMetaTags(model);
                // Remove the existing meta tags and override them below to improve SEO
                htmlMetas.forEach(map -> {
                    String nameAttrValue = map.get(Meta.NAME);
                    if (nameAttrValue != null && existingMetaPosMap.containsKey(nameAttrValue)) {
                        // Descending order of index to remove
                        existingMetaPosMap.get(nameAttrValue)
                            .stream()
                            .sorted(Collections.reverseOrder())
                            .forEach(model::remove);
                    }
                });
            })
            .doOnNext(htmlMetas -> {
                String metaHtml = headMetaBuilder(htmlMetas);
                IModelFactory modelFactory = context.getModelFactory();
                model.add(modelFactory.createText(metaHtml));
            })
            .then();
    }

    /**
     * Find the items in the queue of the {@param #model} that matches the HTML meta tag, and obtain
     * the value of its name attribute and its index in the queue.
     *
     * @param model template model object
     * @return map of name attribute and its index in the queue
     */
    Multimap<String, Integer> lookupMetaTags(IModel model) {
        Multimap<String, Integer> metaTags = ArrayListMultimap.create();
        for (int i = 0; i < model.size(); i++) {
            ITemplateEvent templateEvent = model.get(i);
            if (templateEvent instanceof IText textNode) {
                Matcher matcher = META_PATTERN.matcher(textNode.getText());
                if (matcher.find()) {
                    String nameAttribute = matcher.group(1);
                    metaTags.put(nameAttribute, i);
                }
            }
        }
        return metaTags;
    }

    static List<Map<String, String>> excerptToMetaDescriptionIfAbsent(
        List<Map<String, String>> htmlMetas,
        String excerpt) {
        String excerptNullSafe = StringUtils.defaultString(excerpt);
        final String excerptSafe = HtmlUtils.htmlEscape(excerptNullSafe);
        List<Map<String, String>> metas = new ArrayList<>(defaultIfNull(htmlMetas, List.of()));
        metas.stream()
            .filter(map -> Meta.DESCRIPTION.equals(map.get(Meta.NAME)))
            .distinct()
            .findFirst()
            .ifPresentOrElse(map ->
                    map.put(Meta.CONTENT, defaultIfBlank(map.get(Meta.CONTENT), excerptSafe)),
                () -> {
                    Map<String, String> map = new HashMap<>();
                    map.put(Meta.NAME, Meta.DESCRIPTION);
                    map.put(Meta.CONTENT, excerptSafe);
                    metas.add(map);
                });
        return metas;
    }

    interface Meta {
        String DESCRIPTION = "description";
        String NAME = "name";
        String CONTENT = "content";
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

    private boolean isPostTemplate(ITemplateContext context) {
        return DefaultTemplateEnum.POST.getValue()
            .equals(context.getVariable(ModelConst.TEMPLATE_ID));
    }

    private boolean isPageTemplate(ITemplateContext context) {
        return DefaultTemplateEnum.SINGLE_PAGE.getValue()
            .equals(context.getVariable(ModelConst.TEMPLATE_ID));
    }
}