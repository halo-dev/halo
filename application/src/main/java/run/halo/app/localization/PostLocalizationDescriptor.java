package run.halo.app.localization;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.Extension;
import run.halo.app.extension.GroupVersionKind;

/**
 * Declares the public display fields of {@code Post} resources.
 *
 * @since 2.28.0
 */
@Component
public class PostLocalizationDescriptor implements ResourceLocalizationDescriptor {
    @Override
    public GroupVersionKind supports() {
        return Post.GVK;
    }

    @Override
    public List<LocalizableField> fields() {
        return Stream.concat(
                        List.of(
                                new LocalizableField("cover", "/spec/cover", LocalizableField.Format.PLAIN_TEXT),
                                new LocalizableField("title", "/spec/title", LocalizableField.Format.PLAIN_TEXT),
                                new LocalizableField("excerpt", "/status/excerpt", LocalizableField.Format.PLAIN_TEXT),
                                new LocalizableField("content.html", null, LocalizableField.Format.HTML),
                                new LocalizableField("content.raw", null, LocalizableField.Format.RAW_CONTENT))
                                .stream(),
                        ContentSeoFields.fields().stream())
                .toList();
    }

    @Override
    public List<LocalizableDocument> documents() {
        return List.of(new LocalizableDocument("content.html", "content.raw", "HTML", null));
    }

    @Override
    public Mono<Map<String, String>> resolveFields(Extension resource, Map<String, String> extracted) {
        var result = new LinkedHashMap<>(extracted);
        result.putAll(ContentSeoFields.extract(resource));
        return Mono.just(result);
    }
}
