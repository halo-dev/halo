package run.halo.app.core.attachment;

import static org.thymeleaf.templatemode.TemplateMode.HTML;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.ElementNames;
import org.thymeleaf.model.IAttribute;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.MatchingElementName;
import org.thymeleaf.spring6.context.SpringContextUtils;
import org.thymeleaf.spring6.context.webflux.SpringWebFluxThymeleafRequestContext;
import reactor.core.publisher.Mono;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.theme.dialect.ElementTagPostProcessor;

@Slf4j
@Component
class ThumbnailImgTagPostProcessor implements ElementTagPostProcessor {

    private final MatchingElementName matchingElementName;

    private final ExternalUrlSupplier externalUrlSupplier;

    public ThumbnailImgTagPostProcessor(ExternalUrlSupplier externalUrlSupplier) {
        this.externalUrlSupplier = externalUrlSupplier;
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

        if (imageUri.isAbsolute()) {
            // check if the uri is belonged to current site
            var requestContext = SpringContextUtils.getRequestContext(context);
            if (!(requestContext instanceof SpringWebFluxThymeleafRequestContext wrc)) {
                log.debug("Skip processing img tag with absolute url: {}, "
                    + "because the request context is not webflux", imageUri);
                return Mono.empty();
            }
            var externalUri = externalUrlSupplier.get();
            if (!externalUri.isAbsolute()) {
                externalUri = wrc.getServerWebExchange().getRequest().getURI();
            }
            if (!Objects.equals(externalUri.getAuthority(), imageUri.getAuthority())) {
                log.debug("""
                    Skip processing img tag with external absolute url: {} because \
                    the url does not belong to the current site\
                    """, imageUri);
                return Mono.empty();
            }
        }

        var fileSuffix = FilenameUtils.getExtension(imageUri.getPath());
        if (!ThumbnailUtils.isSupportedImage(fileSuffix)) {
            log.debug("Skip processing img tag with unsupported image suffix: {}", fileSuffix);
            return Mono.empty();
        }

        // build thumbnails
        var thumbnails = ThumbnailUtils.buildSrcsetMap(imageUri);
        if (CollectionUtils.isEmpty(thumbnails)) {
            log.debug("Skip processing img tag because the image is not supported: {}", imageUri);
            return Mono.empty();
        }
        var modelFactory = context.getModelFactory();
        tag = modelFactory.setAttribute(tag, "size", """
            (max-width: 400px) 400px, \
            (max-width: 800px) 800px, \
            (max-width: 1200px) 1200px, \
            (max-width: 1600px) 1600px\
            """);

        var srcset = thumbnails.keySet().stream()
            .map(size -> {
                var uri = thumbnails.get(size);
                return uri + " " + size.getWidth() + "w";
            })
            .collect(Collectors.joining(", "));
        tag = modelFactory.setAttribute(tag, "srcset", srcset);
        return Mono.just(tag);
    }

}
