package run.halo.app.utils;

import cn.hutool.core.lang.Assert;
import com.vladsch.flexmark.ext.attributes.AttributesExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.emoji.EmojiImageType;
import com.vladsch.flexmark.ext.emoji.EmojiShortcutType;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

/**
 * Compare the rendering result of FootnoteNodeRendererInterceptor
 * and <a href="https://github.com/markdown-it/markdown-it-footnote">markdown-it-footnote</a>.
 * You can view <code>markdown-it-footnote's</code> rendering HTML results on this
 * link <a href="https://markdown-it.github.io/">markdown-it-footnote example page</a>.
 *
 * @author guqing
 * @date 2021-06-26
 */
public class FootnoteNodeRendererInterceptorTest {
    private static final DataHolder OPTIONS =
        new MutableDataSet().set(Parser.EXTENSIONS, Arrays.asList(EmojiExtension.create(),
            FootnoteExtension.create()))
            .set(HtmlRenderer.SOFT_BREAK, "<br />\n")
            .set(FootnoteExtension.FOOTNOTE_BACK_REF_STRING, "↩︎")
            .set(EmojiExtension.USE_SHORTCUT_TYPE, EmojiShortcutType.EMOJI_CHEAT_SHEET)
            .set(EmojiExtension.USE_IMAGE_TYPE, EmojiImageType.UNICODE_ONLY);
    private static final Parser PARSER = Parser.builder(OPTIONS).build();

    private static final HtmlRenderer RENDERER = HtmlRenderer.builder(OPTIONS).build();

    private String renderHtml(String markdown) {
        FootnoteNodeRendererInterceptor.doDelegationMethod();

        Node document = PARSER.parse(markdown);

        return RENDERER.render(document);
    }

    @Test
    public void duplicatedTest() {
        // duplicated
        String markdown = "text [^footnote] embedded.\n"
            + "\n"
            + "[^footnote]: footnote text\n"
            + "with continuation\n"
            + "\n"
            + "[^footnote]: duplicated footnote text\n"
            + "with continuation";
        String s = renderHtml(markdown);
        System.out.println(s);
        Assert.isTrue(StringUtils.equals(s, "<p>text <sup class=\"footnote-ref\"><a id=\"fnref1\""
            + " href=\"#fn1\">[1]</a></sup> embedded.</p>\n"
            + "<hr class=\"footnotes-sep\" />\n"
            + "<section class=\"footnotes\">\n"
            + "<ol class=\"footnotes-list\">\n"
            + "<li id=\"fn1\" class=\"footnote-item\">\n"
            + "<p>footnote text<br />\n"
            + "with continuation <a href=\"#fnref1\" class=\"footnote-backref\">↩︎</a></p>\n"
            + "</li>\n"
            + "</ol>\n"
            + "</section>\n"));
    }

    @Test
    public void nestedTest() {
        // nested
        String markdown = "text [^footnote] embedded.\n"
            + "\n"
            + "[^footnote]: footnote text with [^another] embedded footnote\n"
            + "with continuation\n"
            + "\n"
            + "[^another]: footnote text\n"
            + "with continuation";
        String s = renderHtml(markdown);
        Assert.isTrue(StringUtils.equals(s, "<p>text <sup class=\"footnote-ref\"><a id=\"fnref1\""
            + " href=\"#fn1\">[1]</a></sup> embedded.</p>\n"
            + "<hr class=\"footnotes-sep\" />\n"
            + "<section class=\"footnotes\">\n"
            + "<ol class=\"footnotes-list\">\n"
            + "<li id=\"fn1\" class=\"footnote-item\">\n"
            + "<p>footnote text with <sup class=\"footnote-ref\"><a id=\"fnref2\" "
            + "href=\"#fn2\">[2]</a></sup> embedded footnote<br />\n"
            + "with continuation <a href=\"#fnref1\" class=\"footnote-backref\">↩︎</a></p>\n"
            + "</li>\n"
            + "<li id=\"fn2\" class=\"footnote-item\">\n"
            + "<p>footnote text<br />\n"
            + "with continuation <a href=\"#fnref2\" class=\"footnote-backref\">↩︎</a></p>\n"
            + "</li>\n"
            + "</ol>\n"
            + "</section>\n"));
    }

    @Test
    public void circularTest() {
        // circular
        String markdown = "text [^footnote] embedded.\n"
            + "\n"
            + "[^footnote]: footnote text with [^another] embedded footnote\n"
            + "with continuation\n"
            + "\n"
            + "[^another]: footnote text with [^another] embedded footnote\n"
            + "with continuation";
        String s = renderHtml(markdown);
        Assert.isTrue(StringUtils.equals(s, "<p>text <sup class=\"footnote-ref\"><a id=\"fnref1\""
            + " href=\"#fn1\">[1]</a></sup> embedded.</p>\n"
            + "<hr class=\"footnotes-sep\" />\n"
            + "<section class=\"footnotes\">\n"
            + "<ol class=\"footnotes-list\">\n"
            + "<li id=\"fn1\" class=\"footnote-item\">\n"
            + "<p>footnote text with <sup class=\"footnote-ref\"><a id=\"fnref2\" "
            + "href=\"#fn2\">[2]</a></sup> embedded footnote<br />\n"
            + "with continuation <a href=\"#fnref1\" class=\"footnote-backref\">↩︎</a></p>\n"
            + "</li>\n"
            + "<li id=\"fn2\" class=\"footnote-item\">\n"
            + "<p>footnote text with <sup class=\"footnote-ref\"><a id=\"fnref2:1\" "
            + "href=\"#fn2\">[2:1]</a></sup> embedded footnote<br />\n"
            + "with continuation <a href=\"#fnref2:1\" class=\"footnote-backref\">↩︎</a></p>\n"
            + "</li>\n"
            + "</ol>\n"
            + "</section>\n"));
    }

    @Test
    public void compoundTest() {
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
        String s = renderHtml(markdown);
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
            + "<a href=\"#fnref1\" class=\"footnote-backref\">↩︎</a>\n"
            + "</li>\n"
            + "</ol>\n"
            + "</section>\n"));
    }

    @Test
    public void notFootnoteTest() {
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
        String s = renderHtml(markdown);
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
    public void unusedFootnotesTest() {
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
        String s = renderHtml(markdown);
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
            + "<a href=\"#fnref1\" class=\"footnote-backref\">↩︎</a>\n"
            + "</li>\n"
            + "</ol>\n"
            + "</section>\n"));
    }

    @Test
    public void undefinedFootnotesTest() {
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
        String s = renderHtml(markdown);
        Assert.isTrue(StringUtils
            .equals(s, "<p>This paragraph has a footnote[^<strong>footnote</strong>].</p>\n"));
    }

    @Test
    public void footnoteNumbersOrderTest() {
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
        String s = renderHtml(markdown);
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
            + "<a href=\"#fnref1\" class=\"footnote-backref\">↩︎</a>\n"
            + "</li>\n"
            + "<li id=\"fn2\" class=\"footnote-item\">\n"
            + "<p>This is the body of the unused footnote.<br />\n"
            + "with continuation text. Inline <em>italic</em> and<br />\n"
            + "<strong>bold</strong>. <a href=\"#fnref2\" class=\"footnote-backref\">↩︎"
            + "</a></p>\n"
            + "</li>\n"
            + "</ol>\n"
            + "</section>\n"));
    }

    @Test
    public void containOtherReferencesTest() {
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
        String s = renderHtml(markdown);
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
            + "<a href=\"#fnref1\" class=\"footnote-backref\">↩︎</a>\n"
            + "</li>\n"
            + "<li id=\"fn2\" class=\"footnote-item\">\n"
            + "<p>This is the body of a nested footnote.<br />\n"
            + "with continuation text. Inline <em>italic</em> and<br />\n"
            + "<strong>bold</strong>.<br />\n"
            + ". <a href=\"#fnref2\" class=\"footnote-backref\">↩︎</a></p>\n"
            + "</li>\n"
            + "</ol>\n"
            + "</section>\n"));
    }
}
