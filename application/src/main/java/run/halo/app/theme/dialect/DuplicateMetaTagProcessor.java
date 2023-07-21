package run.halo.app.theme.dialect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.ITemplateEvent;
import org.thymeleaf.model.IText;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;

/**
 * <p>This processor will remove the duplicate meta tag with the same name in head tag and only
 * keep the last one.</p>
 * <p>This processor will be executed last.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
@Order
@Component
@AllArgsConstructor
public class DuplicateMetaTagProcessor implements TemplateHeadProcessor {
    static final Pattern META_PATTERN = Pattern.compile("<meta[^>]+?name=\"([^\"]+)\"[^>]*>\\n*");

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model,
        IElementModelStructureHandler structureHandler) {
        IModel newModel = context.getModelFactory().createModel();

        Map<String, IndexedModel> uniqueMetaTags = new LinkedHashMap<>();
        List<IndexedModel> otherModel = new ArrayList<>();
        for (int i = 0; i < model.size(); i++) {
            ITemplateEvent templateEvent = model.get(i);
            // If the current node is a text node, it is processed separately.
            // Because the text node may contain multiple meta tags.
            if (templateEvent instanceof IText textNode) {
                String text = textNode.getText();
                Matcher matcher = META_PATTERN.matcher(text);
                while (matcher.find()) {
                    String tagLine = matcher.group(0);
                    String nameAttribute = matcher.group(1);
                    // create a new text node to replace the original text node
                    // replace multiple line breaks with one line break
                    IText metaTagNode = context.getModelFactory()
                        .createText(tagLine.replaceAll("\\n+", "\n"));
                    uniqueMetaTags.put(nameAttribute, new IndexedModel(i, metaTagNode));
                    text = text.replace(tagLine, "");
                }
                // put the rest of the text into the other model
                IText otherText = context.getModelFactory()
                    .createText(text);
                otherModel.add(new IndexedModel(i, otherText));
            } else {
                otherModel.add(new IndexedModel(i, templateEvent));
            }
        }

        otherModel.addAll(uniqueMetaTags.values());
        otherModel.stream().sorted(Comparator.comparing(IndexedModel::index))
            .map(IndexedModel::templateEvent)
            .forEach(newModel::add);

        model.reset();
        model.addModel(newModel);
        return Mono.empty();
    }

    record IndexedModel(int index, ITemplateEvent templateEvent) {
    }
}
