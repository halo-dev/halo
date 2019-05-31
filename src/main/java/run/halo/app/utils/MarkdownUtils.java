package run.halo.app.utils;

import org.apache.commons.lang3.StringUtils;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.ext.ins.InsExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.text.TextContentRenderer;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import run.halo.app.model.support.HaloConst;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Markdown utils
 *
 * @author ryanwang
 * @date : 2018/11/14
 */
public class MarkdownUtils {


    /**
     * commonmark-java extension for autolinking
     */
    private static final Set<Extension> EXTENSIONS_AUTO_LINK = Collections.singleton(AutolinkExtension.create());

    /**
     * commonmark-java extension for strikethrough
     */
    private static final Set<Extension> EXTENSIONS_STRIKETHROUGH = Collections.singleton(StrikethroughExtension.create());

    /**
     * commonmark-java extension for tables
     */
    private static final Set<Extension> EXTENSIONS_TABLES = Collections.singleton(TablesExtension.create());

    /**
     * commonmark-java extension for adding id attributes to h tags
     */
    private static final Set<Extension> EXTENSIONS_HEADING_ANCHOR = Collections.singleton(HeadingAnchorExtension.create());

    /**
     * commonmark-java extension for &lt;ins&gt; (underline)
     */
    private static final Set<Extension> EXTENSIONS_INS = Collections.singleton(InsExtension.create());

    /**
     * commonmark-java extension for YAML front matter
     */
    private static final Set<Extension> EXTENSIONS_YAML_FRONT_MATTER = Collections.singleton(YamlFrontMatterExtension.create());


    /**
     * Parse Markdown content
     */
    private static final Parser PARSER = Parser.builder()
            .extensions(EXTENSIONS_AUTO_LINK)
            .extensions(EXTENSIONS_STRIKETHROUGH)
            .extensions(EXTENSIONS_TABLES)
            .extensions(EXTENSIONS_HEADING_ANCHOR)
            .extensions(EXTENSIONS_INS)
            .extensions(EXTENSIONS_YAML_FRONT_MATTER)
            .build();

    /**
     * Render HTML content
     */
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder()
            .extensions(EXTENSIONS_AUTO_LINK)
            .extensions(EXTENSIONS_STRIKETHROUGH)
            .extensions(EXTENSIONS_TABLES)
            .extensions(EXTENSIONS_HEADING_ANCHOR)
            .extensions(EXTENSIONS_INS)
            .extensions(EXTENSIONS_YAML_FRONT_MATTER)
            .build();

    /**
     * Render text content
     */
    private static final TextContentRenderer TEXT_CONTENT_RENDERER = TextContentRenderer.builder()
            .extensions(EXTENSIONS_AUTO_LINK)
            .extensions(EXTENSIONS_STRIKETHROUGH)
            .extensions(EXTENSIONS_TABLES)
            .extensions(EXTENSIONS_HEADING_ANCHOR)
            .extensions(EXTENSIONS_INS)
            .extensions(EXTENSIONS_YAML_FRONT_MATTER)
            .build();

    /**
     * Render Markdown content
     *
     * @param content content
     * @return String
     * @see <a href="https://github.com/otale/tale/blob/master/src/main/java/com/tale/utils/TaleUtils.java">TaleUtils.java</a>
     */
    public static String renderMarkdown(String content) {

        final Node document = PARSER.parse(content);
        String renderContent = RENDERER.render(document);

        // render netease music short url
        if (content.contains(HaloConst.NETEASE_MUSIC_PREFIX)) {
            renderContent = content.replaceAll(HaloConst.NETEASE_MUSIC_REG_PATTERN, HaloConst.NETEASE_MUSIC_IFRAME);
        }

        // render bilibili video short url
        if (content.contains(HaloConst.BILIBILI_VIDEO_PREFIX)) {
            renderContent = content.replaceAll(HaloConst.BILIBILI_VIDEO_REG_PATTERN, HaloConst.BILIBILI_VIDEO_IFRAME);
        }

        // render youtube video short url
        if (content.contains(HaloConst.YOUTUBE_VIDEO_PREFIX)) {
            renderContent = content.replaceAll(HaloConst.YOUTUBE_VIDEO_REG_PATTERN, HaloConst.YOUTUBE_VIDEO_IFRAME);
        }

        return renderContent;
    }

    /**
     * Render text content.
     *
     * @param markdownContent markdown content
     * @return text content or empty string if markdown content is blank
     */
    @NonNull
    public static String renderText(@Nullable String markdownContent) {

        if (StringUtils.isBlank(markdownContent)) {
            return "";
        }

        return TEXT_CONTENT_RENDERER.render(PARSER.parse(markdownContent));
    }

    /**
     * Get front-matter
     *
     * @param content content
     * @return Map
     */
    public static Map<String, List<String>> getFrontMatter(String content) {
        final YamlFrontMatterVisitor visitor = new YamlFrontMatterVisitor();
        final Node document = PARSER.parse(content);
        document.accept(visitor);
        return visitor.getData();
    }
}
