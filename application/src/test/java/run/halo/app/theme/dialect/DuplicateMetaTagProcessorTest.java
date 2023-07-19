package run.halo.app.theme.dialect;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Matcher;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DuplicateMetaTagProcessor}.
 *
 * @author guqing
 * @since 2.8.0
 */
class DuplicateMetaTagProcessorTest {

    @Test
    void extractMetaTag() {
        // normal
        String text = "<meta name=\"description\" content=\"a description\"/>";
        Matcher matcher = DuplicateMetaTagProcessor.META_PATTERN.matcher(text);
        assertThat(matcher.find()).isTrue();
        assertThat(matcher.group(1)).isEqualTo("description");

        // name and content are not in the general order
        text = "<meta content=\"K1,K2\" name=\"keywords\"/>";
        matcher = DuplicateMetaTagProcessor.META_PATTERN.matcher(text);
        assertThat(matcher.find()).isTrue();
        assertThat(matcher.group(1)).isEqualTo("keywords");

        // no closing slash
        text = "<meta content=\"K1,K2\" name=\"keywords\">";
        matcher = DuplicateMetaTagProcessor.META_PATTERN.matcher(text);
        assertThat(matcher.find()).isTrue();
        assertThat(matcher.group(1)).isEqualTo("keywords");

        // multiple line breaks and other stuff
        text = """
            <meta content="全局 Head description" name="description" />
            
            <style>
              .moment .momemt-content pre.notranslate {
                background: #f3f3f3;
                color: #444;
              }
            </style>
            """;
        matcher = DuplicateMetaTagProcessor.META_PATTERN.matcher(text);
        assertThat(matcher.find()).isTrue();
        assertThat(matcher.group(1)).isEqualTo("description");
    }
}
