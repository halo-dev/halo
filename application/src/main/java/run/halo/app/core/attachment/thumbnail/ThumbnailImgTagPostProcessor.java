package run.halo.app.core.attachment.thumbnail;

import static org.thymeleaf.templatemode.TemplateMode.HTML;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.ElementNames;
import org.thymeleaf.model.IAttribute;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.MatchingElementName;
import reactor.core.publisher.Mono;
import run.halo.app.theme.dialect.ElementTagPostProcessor;

@Slf4j
@Component
class ThumbnailImgTagPostProcessor implements ElementTagPostProcessor {

    private static final String DEFAULT_SIZES = """
        (max-width: 640px) 94vw, \
        (max-width: 768px) 92vw, \
        (max-width: 1024px) 88vw, \
        min(800px, 85vw)\
        """;

    private final MatchingElementName matchingElementName;

    private final ThumbnailService thumbnailService;

    public ThumbnailImgTagPostProcessor(ThumbnailService thumbnailService) {
        this.thumbnailService = thumbnailService;
        this.matchingElementName =
            MatchingElementName.forElementName(HTML, ElementNames.forHTMLName("img"));
    }


    @Override
    public Mono<IProcessableElementTag> process(ITemplateContext context,
        IProcessableElementTag tag) {
        if (!matchingElementName.matches(tag.getElementDefinition().getElementName())) {
            return Mono.empty();
        }
        if (tag.hasAttribute("srcset")) {
            return Mono.empty();
        }
        var srcValue = Optional.ofNullable(tag.getAttribute("src"))
            .map(IAttribute::getValue)
            .filter(StringUtils::hasText)
            .map(URI::create);
        if (srcValue.isEmpty()) {
            log.debug("Skip processing img tag without src attribute");
            return Mono.empty();
        }
        // get img tag
        var imageUri = srcValue.get();

        return thumbnailService.get(imageUri)
            .filter(Predicate.not(Map::isEmpty))
            .map(thumbnails -> {
                var modelFactory = context.getModelFactory();
                var newTag = tag;
                if (!newTag.hasAttribute("sizes")) {
                    newTag = modelFactory.setAttribute(newTag, "sizes", DEFAULT_SIZES);
                }
                var srcset = thumbnails.keySet().stream()
                    .map(size -> {
                        var uri = thumbnails.get(size);
                        return uri + " " + size.getWidth() + "w";
                    })
                    .collect(Collectors.joining(", "));
                newTag = modelFactory.setAttribute(newTag, "srcset", srcset);
                return newTag;
            });
    }

}
