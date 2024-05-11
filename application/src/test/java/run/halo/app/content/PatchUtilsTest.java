package run.halo.app.content;

import static com.github.difflib.UnifiedDiffUtils.generateUnifiedDiff;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Patch;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PatchUtils}.
 *
 * @author guqing
 * @since 2.16.0
 */
class PatchUtilsTest {

    @Test
    void breakHtml() {
        var result = PatchUtils.breakHtml("abc\nadasdas\nafasdfdsa");
        assertThat(result).containsExactly("abc", "adasdas", "afasdfdsa");

        result = PatchUtils.breakHtml("<html>\n<body><p>Line one</p>\n\n</body></html>");
        assertThat(result).containsExactly("<html>", "<body>", "<p>", "Line one", "</p>", "</body>",
            "</html>");
    }

    @Test
    void generateUnifiedDiffForHtml() {
        String originalHtml = """
            <html>
            <body>
              <p>Line one</p>
              <p>Line two</p>
              </body>
            </html>
            """;
        String revisedHtml = """
            <html>
            <body>
              <p>Line one</p>
              <p>Line three</p>
              <p>New line</p>
            </body>
            </html>
            """;
        var originalLines = PatchUtils.breakHtml(originalHtml);
        Patch<String> patch = DiffUtils.diff(originalLines, PatchUtils.breakHtml(revisedHtml));
        var unifiedDiff = generateUnifiedDiff("left", "right",
            originalLines, patch, 10);
        var highlighted = PatchUtils.highlightDiffChanges(unifiedDiff);
        assertThat(highlighted).isEqualToIgnoringWhitespace("""
            <div class="diff-html-remove">-- left</div>
            <div class="diff-html-add">++ right</div>
            @@ -1,10 +1,13 @@
             <html>
             <body>
             <p>
             Line one
             </p>
             <p>
            <div class="diff-html-remove">Line two</div>
            <div class="diff-html-add">Line three</div>
             </p>
            <div class="diff-html-add"><p></div>
            <div class="diff-html-add">New line</div>
            <div class="diff-html-add"></p></div>
            </body>
            </html>
            """);
    }
}