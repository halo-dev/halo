package run.halo.app.content;

import static org.assertj.core.api.Assertions.assertThat;
import static run.halo.app.content.HtmlThumbnailSrcsetInjector.buildSizesAttr;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

/**
 * Tests for {@link HtmlThumbnailSrcsetInjector}.
 *
 * @author guqing
 * @since 2.19.0
 */
class HtmlThumbnailSrcsetInjectorTest {

    @Test
    void injectSrcset() {
        String html = """
            <div>
              <img src='image1.jpg' alt="test">
              <img src='image2.jpg' srcset='image2-small.jpg \
            480w, image2-large.jpg 800w'>
            </div>
            """;
        var result = HtmlThumbnailSrcsetInjector.injectSrcset(html,
            src -> Mono.just(src + " 480w, " + src + " 800w")).block();
        assertThat(result).isEqualToIgnoringWhitespace("""
            <div>
              <img src="image1.jpg" alt="test" srcset="image1.jpg 480w, image1.jpg 800w"\
                sizes="(max-width: 400px) 400px, (max-width: 800px) 800px,\
                 (max-width: 1200px) 1200px, (max-width: 1600px) 1600px">
              <img src="image2.jpg" srcset="image2-small.jpg 480w, image2-large.jpg 800w">
            </div>
            """);
    }

    @Test
    void buildSizesTest() {
        var sizes = buildSizesAttr();
        assertThat(sizes).isEqualToIgnoringWhitespace("""
            (max-width: 400px) 400px, (max-width: 800px) 800px,
            (max-width: 1200px) 1200px, (max-width: 1600px) 1600px
            """);
    }
}
