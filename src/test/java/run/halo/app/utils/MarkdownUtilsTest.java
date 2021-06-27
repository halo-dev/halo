package run.halo.app.utils;

import cn.hutool.core.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

/**
 * @author zhixiang.yuan
 * @since 2020/07/19 20:22:58
 */
@Slf4j
class MarkdownUtilsTest {

    @Test
    void removeFrontMatter() {
        String markdown = "---\n"
            + "title: \"test remove\"\n"
            + "---";
        Assert.isTrue("".equals(MarkdownUtils.removeFrontMatter(markdown)));

        markdown = "---\n"
            + "title: \"test remove\"\n"
            + "---"
            + "test";
        Assert.isTrue("test".equals(MarkdownUtils.removeFrontMatter(markdown)));

        markdown = "---\n"
            + "title: \"test remove\"\n"
            + "---"
            + "test---";
        Assert.isTrue("test---".equals(MarkdownUtils.removeFrontMatter(markdown)));
    }

    @Test
    void footNotesTest() {
        String markdown1 = "驿外[^1]断桥边，寂寞开无主。已是黄昏独自愁，更着风和雨\n"
            + "[^1]: 驿（yì）外：指荒僻、冷清之地。驿，驿站。";
        String s1 = MarkdownUtils.renderHtml(markdown1);
        Assert.isTrue(StringUtils.isNotBlank(s1));
        String s1Expected = "<p>驿外<sup class=\"footnote-ref\"><a id=\"fnref1\" "
            + "href=\"#fn1\">[1]</a></sup>断桥边，寂寞开无主。已是黄昏独自愁，更着风和雨</p>\n"
            + "<hr class=\"footnotes-sep\" />\n"
            + "<section class=\"footnotes\">\n"
            + "<ol class=\"footnotes-list\">\n"
            + "<li id=\"fn1\" class=\"footnote-item\">\n"
            + "<p>驿（yì）外：指荒僻、冷清之地。驿，驿站。 <a href=\"#fnref1\" class=\"footnote-backref\">↩︎"
            + "</a></p>\n"
            + "</li>\n"
            + "</ol>\n"
            + "</section>\n";
        Assert.isTrue(StringUtils.equals(s1Expected, s1));

        String markdown2 = "Paragraph with a footnote reference[^1]\n"
            + "[^1]: Footnote text added at the bottom of the document";
        String s2 = MarkdownUtils.renderHtml(markdown2);
        String s2Expected = "<p>Paragraph with a footnote reference<sup class=\"footnote-ref\"><a"
            + " id=\"fnref1\" href=\"#fn1\">[1]</a></sup></p>\n"
            + "<hr class=\"footnotes-sep\" />\n"
            + "<section class=\"footnotes\">\n"
            + "<ol class=\"footnotes-list\">\n"
            + "<li id=\"fn1\" class=\"footnote-item\">\n"
            + "<p>Footnote text added at the bottom of the document <a href=\"#fnref1\" "
            + "class=\"footnote-backref\">↩︎</a></p>\n"
            + "</li>\n"
            + "</ol>\n"
            + "</section>\n";
        Assert.isTrue(StringUtils.equals(s2Expected, s2));
    }
}