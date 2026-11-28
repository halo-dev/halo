package run.halo.app.localization;

import java.util.LinkedHashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.content.ContentWrapper;

/**
 * Projects a reconstructed content document for public consumers.
 *
 * <p>The caller must obtain content through its normal service and check access first. This transformer does not read
 * snapshots or own domain services.
 *
 * @since 2.28.0
 */
@Component
@RequiredArgsConstructor
public class ContentLocalizationTransformer {
    private static final String CONTENT_HTML = "content.html";
    private static final String CONTENT_RAW = "content.raw";

    private final ResourceLocalizationService localization;

    /**
     * Localizes the HTML and raw source as one document, preserving the wrapper metadata.
     *
     * @param owner source resource identity
     * @param source reconstructed source content
     * @return isolated display content, or the source when no valid translation applies
     */
    public Mono<ContentWrapper> transform(ResourceReference owner, ContentWrapper source) {
        var fields = new LinkedHashMap<String, String>();
        fields.put(CONTENT_HTML, source.getContent());
        fields.put(CONTENT_RAW, source.getRaw());
        return LocalizationContext.current()
                .filter(ContentLocalizationTransformer::isPublicContext)
                .flatMap(context -> document(source)
                        .flatMap(document -> localization.localizeFields(owner, fields, List.of(document), context)))
                .map(localized -> ContentWrapper.builder()
                        .snapshotName(source.getSnapshotName())
                        .rawType(source.getRawType())
                        .content(localized.get(CONTENT_HTML))
                        .raw(localized.get(CONTENT_RAW))
                        .build())
                .defaultIfEmpty(source);
    }

    private static boolean isPublicContext(LocalizationContext context) {
        if (!context.enabled()) {
            return false;
        }
        return context.scope() == LocalizationScope.PUBLIC_PAGE || context.scope() == LocalizationScope.PUBLIC_API;
    }

    private static Mono<LocalizableDocument> document(ContentWrapper source) {
        var rawType = source.getRawType();
        if (rawType == null) {
            return Mono.empty();
        }
        try {
            return Mono.just(new LocalizableDocument(CONTENT_HTML, CONTENT_RAW, rawType, null));
        } catch (IllegalArgumentException error) {
            return Mono.empty();
        }
    }
}
