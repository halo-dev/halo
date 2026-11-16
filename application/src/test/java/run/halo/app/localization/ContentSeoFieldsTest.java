package run.halo.app.localization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.Metadata;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

class ContentSeoFieldsTest {
    @Test
    void customMetadataIsTranslatedByIdentityAndCannotChangeRobotsOrUrls() {
        var post = source(List.of(
                Map.of("property", "og:description", "content", "分享原文"),
                Map.of("name", "description", "content", "搜索原文"),
                Map.of("name", "robots", "content", "noindex"),
                Map.of("property", "og:url", "content", "https://example.org/source")));
        var fields =
                new PostLocalizationDescriptor().resolveFields(post, Map.of()).block();
        assertThat(fields)
                .containsExactlyInAnyOrderEntriesOf(Map.of("seo.description", "搜索原文", "seo.og:description", "分享原文"));
        var getter = mock(ExtensionGetter.class);
        var provider = mock(ResourceLocalizationProvider.class);
        when(getter.getEnabledExtension(ResourceLocalizationProvider.class)).thenReturn(Mono.just(provider));
        when(getter.getEnabledExtensions(ResourceLocalizationDescriptor.class))
                .thenReturn(Flux.just(new PostLocalizationDescriptor()));
        when(provider.resolveAll(any(), any()))
                .thenReturn(Flux.just(new LocalizationPatch(
                        ResourceReference.from(post),
                        Locale.ENGLISH,
                        List.of(
                                field("seo.description", "搜索原文", "Search description"),
                                field("seo.og:description", "分享原文", "Sharing description"),
                                field("seo.robots", "noindex", "index"),
                                field("seo.og:url", "https://example.org/source", "https://other.org")))));
        var service = new DefaultResourceLocalizationService(
                getter, JsonMapper.builder().build());
        var context = new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_PAGE);
        var translated = service.localize(post, context).block();
        assertThat(translated.getSpec().getHtmlMetas())
                .contains(
                        Map.of("property", "og:description", "content", "Sharing description"),
                        Map.of("name", "description", "content", "Search description"),
                        Map.of("name", "robots", "content", "noindex"),
                        Map.of("property", "og:url", "content", "https://example.org/source"));
        assertThat(post.getSpec().getHtmlMetas().get(1).get("content")).isEqualTo("搜索原文");
    }

    @Test
    void ambiguousRepeatedMetadataIsNotOfferedForTranslation() {
        var post = source(
                List.of(Map.of("name", "description", "content", "一"), Map.of("name", "description", "content", "二")));
        assertThat(ContentSeoFields.extract(post)).isEmpty();
    }

    private static LocalizationPatch.Field field(String key, String source, String value) {
        return new LocalizationPatch.Field(key, LocalizationHash.of(source), value, LocalizationPatch.State.APPROVED);
    }

    private static Post source(List<Map<String, String>> metas) {
        var post = new Post();
        var metadata = new Metadata();
        metadata.setName("post");
        post.setMetadata(metadata);
        var spec = new Post.PostSpec();
        spec.setTitle("title");
        spec.setHtmlMetas(metas);
        post.setSpec(spec);
        return post;
    }
}
