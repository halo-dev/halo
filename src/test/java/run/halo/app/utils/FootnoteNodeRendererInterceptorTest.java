package run.halo.app.utils;

import cn.hutool.core.lang.Assert;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

/**
 * Compare the rendering result of FootnoteNodeRendererInterceptor
 * and <a href="https://github.com/markdown-it/markdown-it-footnote">markdown-it-footnote</a>.
 * You can view <code>markdown-it-footnote's</code> rendering HTML results on this
 * link <a href="https://markdown-it.github.io/">markdown-it-footnote example page</>.
 *
 * @author guqing
 * @date 2021-06-26
 */
public class FootnoteNodeRendererInterceptorTest {

    @Test
    public void test() {
        // duplicated
        String markdown = "text [^footnote] embedded.\n"
            + "\n"
            + "[^footnote]: footnote text\n"
            + "with continuation\n"
            + "\n"
            + "[^footnote]: duplicated footnote text\n"
            + "with continuation";
        String s = MarkdownUtils.renderHtml(markdown);
        Assert.isTrue(StringUtils.equals(s, "<p>text <sup class=\"footnote-ref\"><a id=\"fnref1\""
            + " href=\"#fn1\">[1]</a></sup> embedded.</p>\n"
            + "<hr class=\"footnotes-sep\" />\n"
            + "<section class=\"footnotes\">\n"
            + "<ol class=\"footnotes-list\">\n"
            + "<li id=\"fn1\" class=\"footnote-item\">\n"
            + "<p>footnote text<br />\n"
            + "with continuation <a href=\"#fnref1\" class=\"footnote-backref\">&#8617;</a></p>\n"
            + "</li>\n"
            + "</ol>\n"
            + "</section>\n"));
    }

    @Test
    public void test2() {
        // nested
        String markdown = "text [^footnote] embedded.\n"
            + "\n"
            + "[^footnote]: footnote text with [^another] embedded footnote\n"
            + "with continuation\n"
            + "\n"
            + "[^another]: footnote text\n"
            + "with continuation";
        String s = MarkdownUtils.renderHtml(markdown);
        Assert.isTrue(StringUtils.equals(s, "<p>text <sup class=\"footnote-ref\"><a id=\"fnref1\""
            + " href=\"#fn1\">[1]</a></sup> embedded.</p>\n"
            + "<hr class=\"footnotes-sep\" />\n"
            + "<section class=\"footnotes\">\n"
            + "<ol class=\"footnotes-list\">\n"
            + "<li id=\"fn1\" class=\"footnote-item\">\n"
            + "<p>footnote text with <sup class=\"footnote-ref\"><a id=\"fnref2\" "
            + "href=\"#fn2\">[2]</a></sup> embedded footnote<br />\n"
            + "with continuation <a href=\"#fnref1\" class=\"footnote-backref\">&#8617;</a></p>\n"
            + "</li>\n"
            + "<li id=\"fn2\" class=\"footnote-item\">\n"
            + "<p>footnote text<br />\n"
            + "with continuation <a href=\"#fnref2\" class=\"footnote-backref\">&#8617;</a></p>\n"
            + "</li>\n"
            + "</ol>\n"
            + "</section>\n"));
    }

    @Test
    public void test3() {
        // circular
        String markdown = "text [^footnote] embedded.\n"
            + "\n"
            + "[^footnote]: footnote text with [^another] embedded footnote\n"
            + "with continuation\n"
            + "\n"
            + "[^another]: footnote text with [^another] embedded footnote\n"
            + "with continuation";
        String s = MarkdownUtils.renderHtml(markdown);
        Assert.isTrue(StringUtils.equals(s, "<p>text <sup class=\"footnote-ref\"><a id=\"fnref1\""
            + " href=\"#fn1\">[1]</a></sup> embedded.</p>\n"
            + "<hr class=\"footnotes-sep\" />\n"
            + "<section class=\"footnotes\">\n"
            + "<ol class=\"footnotes-list\">\n"
            + "<li id=\"fn1\" class=\"footnote-item\">\n"
            + "<p>footnote text with <sup class=\"footnote-ref\"><a id=\"fnref2\" "
            + "href=\"#fn2\">[2]</a></sup> embedded footnote<br />\n"
            + "with continuation <a href=\"#fnref1\" class=\"footnote-backref\">&#8617;</a></p>\n"
            + "</li>\n"
            + "<li id=\"fn2\" class=\"footnote-item\">\n"
            + "<p>footnote text with <sup class=\"footnote-ref\"><a id=\"fnref2:1\" "
            + "href=\"#fn2\">[2:1]</a></sup> embedded footnote<br />\n"
            + "with continuation <a href=\"#fnref2:1\" class=\"footnote-backref\">&#8617;</a></p>\n"
            + "</li>\n"
            + "</ol>\n"
            + "</section>\n"));
    }

    @Test
    public void test4() {
        // compound
        String markdown = "This paragraph has a footnote[^footnote].\n"
            + "\n"
            + "[^footnote]: This is the body of the footnote.\n"
            + "with continuation text. Inline _italic_ and \n"
            + "**bold**.\n"
            + "\n"
            + "    Multiple paragraphs are supported as other \n"
            + "    markdown elements such as lists.\n"
            + "    \n"
            + "    - item 1\n"
            + "    - item 2\n"
            + ".";
        String s = MarkdownUtils.renderHtml(markdown);
        Assert.isTrue(StringUtils.equals(s, "<p>This paragraph has a footnote<sup "
            + "class=\"footnote-ref\"><a id=\"fnref1\" href=\"#fn1\">[1]</a></sup>.</p>\n"
            + "<hr class=\"footnotes-sep\" />\n"
            + "<section class=\"footnotes\">\n"
            + "<ol class=\"footnotes-list\">\n"
            + "<li id=\"fn1\" class=\"footnote-item\">\n"
            + "<p>This is the body of the footnote.<br />\n"
            + "with continuation text. Inline <em>italic</em> and<br />\n"
            + "<strong>bold</strong>.</p>\n"
            + "<p>Multiple paragraphs are supported as other<br />\n"
            + "markdown elements such as lists.</p>\n"
            + "<ul>\n"
            + "<li>item 1</li>\n"
            + "<li>item 2<br />\n"
            + ".</li>\n"
            + "</ul>\n"
            + "<a href=\"#fnref1\" class=\"footnote-backref\">&#8617;</a>\n"
            + "</li>\n"
            + "</ol>\n"
            + "</section>\n"));
    }

    @Test
    public void test5() {
        // Not a footnote nor a footnote definition if space between [ and ^.
        String markdown = "This paragraph has no footnote[ ^footnote].\n"
            + "\n"
            + "[ ^footnote]: This is the body of the footnote.\n"
            + "with continuation text. Inline _italic_ and \n"
            + "**bold**.\n"
            + "\n"
            + "    Multiple paragraphs are supported as other \n"
            + "    markdown elements such as lists.\n"
            + "    \n"
            + "    - item 1\n"
            + "    - item 2\n"
            + ".";
        String s = MarkdownUtils.renderHtml(markdown);
        Assert.isTrue(StringUtils.equals(s, "<p>This paragraph has no footnote[ ^footnote].</p>\n"
            + "<p>[ ^footnote]: This is the body of the footnote.<br />\n"
            + "with continuation text. Inline <em>italic</em> and<br />\n"
            + "<strong>bold</strong>.</p>\n"
            + "<pre><code>Multiple paragraphs are supported as other \n"
            + "markdown elements such as lists.\n"
            + "\n"
            + "- item 1\n"
            + "- item 2\n"
            + "</code></pre>\n"
            + "<p>.</p>\n"));
    }

    @Test
    public void test6() {
        // Unused footnotes are not used and do not show up on the page.
        String markdown = "This paragraph has a footnote[^2].\n"
            + "\n"
            + "[^1]: This is the body of the unused footnote.\n"
            + "with continuation text. Inline _italic_ and \n"
            + "**bold**.\n"
            + "\n"
            + "[^2]: This is the body of the footnote.\n"
            + "with continuation text. Inline _italic_ and \n"
            + "**bold**.\n"
            + "\n"
            + "    Multiple paragraphs are supported as other \n"
            + "    markdown elements such as lists.\n"
            + "    \n"
            + "    - item 1\n"
            + "    - item 2\n"
            + ".";
        String s = MarkdownUtils.renderHtml(markdown);
        Assert.isTrue(StringUtils.equals(s, "<p>This paragraph has a footnote<sup "
            + "class=\"footnote-ref\"><a id=\"fnref1\" href=\"#fn1\">[1]</a></sup>.</p>\n"
            + "<hr class=\"footnotes-sep\" />\n"
            + "<section class=\"footnotes\">\n"
            + "<ol class=\"footnotes-list\">\n"
            + "<li id=\"fn1\" class=\"footnote-item\">\n"
            + "<p>This is the body of the footnote.<br />\n"
            + "with continuation text. Inline <em>italic</em> and<br />\n"
            + "<strong>bold</strong>.</p>\n"
            + "<p>Multiple paragraphs are supported as other<br />\n"
            + "markdown elements such as lists.</p>\n"
            + "<ul>\n"
            + "<li>item 1</li>\n"
            + "<li>item 2<br />\n"
            + ".</li>\n"
            + "</ul>\n"
            + "<a href=\"#fnref1\" class=\"footnote-backref\">&#8617;</a>\n"
            + "</li>\n"
            + "</ol>\n"
            + "</section>\n"));
    }

    @Test
    public void test7() {
        // Undefined footnotes are rendered as if they were text, with emphasis left as is.
        String markdown = "This paragraph has a footnote[^**footnote**].\n"
            + "\n"
            + "[^footnote]: This is the body of the footnote.\n"
            + "with continuation text. Inline _italic_ and \n"
            + "**bold**.\n"
            + "\n"
            + "    Multiple paragraphs are supported as other \n"
            + "    markdown elements such as lists.\n"
            + "    \n"
            + "    - item 1\n"
            + "    - item 2\n"
            + ".";
        String s = MarkdownUtils.renderHtml(markdown);
        Assert.isTrue(StringUtils
            .equals(s, "<p>This paragraph has a footnote[^<strong>footnote</strong>].</p>\n"));
    }

    @Test
    public void test8() {
        // Footnote numbers are assigned in order of their reference in the document.
        String markdown = "This paragraph has a footnote[^2]. Followed by another[^1]. \n"
            + "\n"
            + "[^1]: This is the body of the unused footnote.\n"
            + "with continuation text. Inline _italic_ and \n"
            + "**bold**.\n"
            + "\n"
            + "[^2]: This is the body of the footnote.\n"
            + "with continuation text. Inline _italic_ and \n"
            + "**bold**.\n"
            + "\n"
            + "    Multiple paragraphs are supported as other \n"
            + "    markdown elements such as lists.\n"
            + "    \n"
            + "    - item 1\n"
            + "    - item 2\n"
            + ".";
        String s = MarkdownUtils.renderHtml(markdown);
        Assert.isTrue(StringUtils.equals(s, "<p>This paragraph has a footnote<sup "
            + "class=\"footnote-ref\"><a id=\"fnref1\" href=\"#fn1\">[1]</a></sup>. Followed by "
            + "another<sup class=\"footnote-ref\"><a id=\"fnref2\" href=\"#fn2\">[2]</a></sup>"
            + ".</p>\n"
            + "<hr class=\"footnotes-sep\" />\n"
            + "<section class=\"footnotes\">\n"
            + "<ol class=\"footnotes-list\">\n"
            + "<li id=\"fn1\" class=\"footnote-item\">\n"
            + "<p>This is the body of the footnote.<br />\n"
            + "with continuation text. Inline <em>italic</em> and<br />\n"
            + "<strong>bold</strong>.</p>\n"
            + "<p>Multiple paragraphs are supported as other<br />\n"
            + "markdown elements such as lists.</p>\n"
            + "<ul>\n"
            + "<li>item 1</li>\n"
            + "<li>item 2<br />\n"
            + ".</li>\n"
            + "</ul>\n"
            + "<a href=\"#fnref1\" class=\"footnote-backref\">&#8617;</a>\n"
            + "</li>\n"
            + "<li id=\"fn2\" class=\"footnote-item\">\n"
            + "<p>This is the body of the unused footnote.<br />\n"
            + "with continuation text. Inline <em>italic</em> and<br />\n"
            + "<strong>bold</strong>. <a href=\"#fnref2\" class=\"footnote-backref\">&#8617;"
            + "</a></p>\n"
            + "</li>\n"
            + "</ol>\n"
            + "</section>\n"));
    }

    @Test
    public void test9() {
        // Footnotes can contain references to other footnotes.
        String markdown = "This paragraph has a footnote[^2].  \n"
            + "\n"
            + "[^2]: This is the body of the footnote.\n"
            + "with continuation text. Inline _italic_ and \n"
            + "**bold**.\n"
            + "\n"
            + "    Multiple paragraphs are supported as other \n"
            + "    markdown elements such as lists and footnotes[^1].\n"
            + "    \n"
            + "    - item 1\n"
            + "    - item 2\n"
            + "    \n"
            + "    [^1]: This is the body of a nested footnote.\n"
            + "    with continuation text. Inline _italic_ and \n"
            + "    **bold**.\n"
            + ".";
        String s = MarkdownUtils.renderHtml(markdown);
        Assert.isTrue(StringUtils.equals(s, "<p>This paragraph has a footnote<sup "
            + "class=\"footnote-ref\"><a id=\"fnref1\" href=\"#fn1\">[1]</a></sup>.</p>\n"
            + "<hr class=\"footnotes-sep\" />\n"
            + "<section class=\"footnotes\">\n"
            + "<ol class=\"footnotes-list\">\n"
            + "<li id=\"fn1\" class=\"footnote-item\">\n"
            + "<p>This is the body of the footnote.<br />\n"
            + "with continuation text. Inline <em>italic</em> and<br />\n"
            + "<strong>bold</strong>.</p>\n"
            + "<p>Multiple paragraphs are supported as other<br />\n"
            + "markdown elements such as lists and footnotes<sup class=\"footnote-ref\"><a "
            + "id=\"fnref2\" href=\"#fn2\">[2]</a></sup>.</p>\n"
            + "<ul>\n"
            + "<li>item 1</li>\n"
            + "<li>item 2</li>\n"
            + "</ul>\n"
            + "<a href=\"#fnref1\" class=\"footnote-backref\">&#8617;</a>\n"
            + "</li>\n"
            + "<li id=\"fn2\" class=\"footnote-item\">\n"
            + "<p>This is the body of a nested footnote.<br />\n"
            + "with continuation text. Inline <em>italic</em> and<br />\n"
            + "<strong>bold</strong>.<br />\n"
            + ". <a href=\"#fnref2\" class=\"footnote-backref\">&#8617;</a></p>\n"
            + "</li>\n"
            + "</ol>\n"
            + "</section>\n"));
    }
}
