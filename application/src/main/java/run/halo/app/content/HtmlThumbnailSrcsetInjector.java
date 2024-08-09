package run.halo.app.content;

import java.net.URI;
import java.util.function.Function;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.attachment.ThumbnailService;
import run.halo.app.core.attachment.ThumbnailSize;

@UtilityClass
public class HtmlThumbnailSrcsetInjector {
    static final String SRC = "src";
    static final String SRCSET = "srcset";

    /**
     * Inject srcset attribute to img tags in the given html.
     */
    public static Mono<String> injectSrcset(String html,
        Function<String, Mono<String>> srcSetValueGenerator) {
        Document document = Jsoup.parseBodyFragment(html);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));

        Elements imgTags = document.select("img[src]");
        return Flux.fromIterable(imgTags)
            .filter(element -> {
                String src = element.attr(SRC);
                return !element.hasAttr(SRCSET) && isValidSrc(src);
            })
            .flatMap(img -> {
                String src = img.attr(SRC);
                return srcSetValueGenerator.apply(src)
                    .doOnNext(srcsetValue -> {
                        img.attr(SRCSET, srcsetValue);
                        img.attr("sizes", buildSizesAttr());
                    });
            })
            .then(Mono.fromSupplier(() -> document.body().html()));
    }

    static String buildSizesAttr() {
        var sb = new StringBuilder();
        var delimiter = ", ";
        for (ThumbnailSize size : ThumbnailSize.values()) {
            sb.append("(max-width: ").append(size.getWidth()).append("px) ")
                .append(size.getWidth())
                .append("px")
                .append(delimiter);
        }
        sb.deleteCharAt(sb.length() - delimiter.length());
        return sb.toString();
    }

    /**
     * Generate srcset attribute value for the given src.
     */
    public static Mono<String> generateSrcset(URI src, ThumbnailService thumbnailService) {
        return Flux.fromArray(ThumbnailSize.values())
            .flatMap(size -> thumbnailService.generate(src, size)
                .map(thumbnail -> thumbnail.toString() + " " + size.getWidth() + "w")
            )
            .collect(StringBuilder::new, (builder, srcsetValue) -> {
                if (!builder.isEmpty()) {
                    builder.append(", ");
                }
                builder.append(srcsetValue);
            })
            .map(StringBuilder::toString)
            .defaultIfEmpty(StringUtils.EMPTY);
    }

    private static boolean isValidSrc(String src) {
        if (StringUtils.isBlank(src)) {
            return false;
        }
        try {
            URI.create(src);
            return true;
        } catch (IllegalArgumentException e) {
            // ignore
        }
        return false;
    }
}
