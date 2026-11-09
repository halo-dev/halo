package run.halo.app.theme.dialect;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.thymeleaf.model.AttributeValueQuotes.DOUBLE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.ITemplateEvent;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.SinglePageFinder;
import run.halo.app.theme.router.ModelConst;

/**
 * The <code>head</code> html snippet injection processor for content template such as post and page.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@Order(1)
@AllArgsConstructor
public class ContentTemplateHeadProcessor implements TemplateHeadProcessor {
    private static final String POST_NAME_VARIABLE = "name";
    private static final Pattern VALID_ATTRIBUTE_NAME = Pattern.compile("[a-zA-Z][a-zA-Z0-9_.:-]*");
    private final PostFinder postFinder;
    private final SinglePageFinder singlePageFinder;

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model, IElementModelStructureHandler structureHandler) {
        Mono<String> nameMono = Mono.justOrEmpty((String) context.getVariable(POST_NAME_VARIABLE));

        Mono<List<Map<String, String>>> htmlMetasMono = Mono.empty();
        if (isPostTemplate(context)) {
            htmlMetasMono = nameMono.flatMap(postFinder::getByName).map(post -> {
                List<Map<String, String>> htmlMetas = post.getSpec().getHtmlMetas();
                String excerpt =
                        post.getStatus() == null ? null : post.getStatus().getExcerpt();
                return excerptToMetaDescriptionIfAbsent(htmlMetas, excerpt);
            });
        } else if (isPageTemplate(context)) {
            htmlMetasMono = nameMono.flatMap(singlePageFinder::getByName).map(page -> {
                List<Map<String, String>> htmlMetas = page.getSpec().getHtmlMetas();
                String excerpt =
                        page.getStatus() == null ? null : page.getStatus().getExcerpt();
                return excerptToMetaDescriptionIfAbsent(htmlMetas, excerpt);
            });
        }

        return htmlMetasMono
                .doOnNext(htmlMetas ->
                        buildMetas(context.getModelFactory(), htmlMetas).forEach(model::add))
                .then();
    }

    static List<Map<String, String>> excerptToMetaDescriptionIfAbsent(
            List<Map<String, String>> htmlMetas, String excerpt) {
        String excerptNullSafe = StringUtils.defaultString(excerpt);
        List<Map<String, String>> metas = new ArrayList<>(defaultIfNull(htmlMetas, List.of()));
        metas.stream()
                .filter(Objects::nonNull)
                .filter(map -> Meta.DESCRIPTION.equals(map.get(Meta.NAME)))
                .distinct()
                .findFirst()
                .ifPresentOrElse(
                        map -> map.put(Meta.CONTENT, defaultIfBlank(map.get(Meta.CONTENT), excerptNullSafe)), () -> {
                            Map<String, String> map = new HashMap<>();
                            map.put(Meta.NAME, Meta.DESCRIPTION);
                            map.put(Meta.CONTENT, excerptNullSafe);
                            metas.add(map);
                        });
        return metas;
    }

    interface Meta {
        String DESCRIPTION = "description";
        String NAME = "name";
        String CONTENT = "content";
    }

    private List<ITemplateEvent> buildMetas(IModelFactory modelFactory, List<Map<String, String>> metas) {
        return metas.stream()
                .map(ContentTemplateHeadProcessor::sanitizeAttributes)
                .filter(Objects::nonNull)
                .map(attributes -> modelFactory.createStandaloneElementTag("meta", attributes, DOUBLE, false, true))
                .collect(Collectors.toList());
    }

    /**
     * Attribute names and values of {@code htmlMetas} come from user content, so they must not be written into the page
     * verbatim. Drop the whole meta tag if any attribute name is not a valid HTML attribute name, and escape every
     * attribute value to prevent breaking out of the tag.
     */
    private static Map<String, String> sanitizeAttributes(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return null;
        }
        var sanitized = new LinkedHashMap<String, String>(attributes.size());
        for (var entry : attributes.entrySet()) {
            var name = entry.getKey();
            if (name == null || !VALID_ATTRIBUTE_NAME.matcher(name).matches()) {
                return null;
            }
            var value = entry.getValue();
            sanitized.put(name, value == null ? null : HtmlUtils.htmlEscape(value));
        }
        return sanitized;
    }

    private boolean isPostTemplate(ITemplateContext context) {
        return DefaultTemplateEnum.POST.getValue().equals(context.getVariable(ModelConst.TEMPLATE_ID));
    }

    private boolean isPageTemplate(ITemplateContext context) {
        return DefaultTemplateEnum.SINGLE_PAGE.getValue().equals(context.getVariable(ModelConst.TEMPLATE_ID));
    }
}
