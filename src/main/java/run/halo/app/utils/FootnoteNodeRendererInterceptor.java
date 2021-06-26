package run.halo.app.utils;

import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.LinkNodeBase;
import com.vladsch.flexmark.ext.footnotes.Footnote;
import com.vladsch.flexmark.ext.footnotes.FootnoteBlock;
import com.vladsch.flexmark.ext.footnotes.internal.FootnoteNodeRenderer;
import com.vladsch.flexmark.ext.footnotes.internal.FootnoteOptions;
import com.vladsch.flexmark.ext.footnotes.internal.FootnoteRepository;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.RenderingPhase;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.VisitHandler;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import java.lang.reflect.Field;
import java.util.Locale;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.yaml.snakeyaml.nodes.SequenceNode;

/**
 * <code>Flexmark</code> footnote node render interceptor.
 * Delegate the render method to intercept the FootNoteNodeRender by ByteBuddy runtime.
 *
 * @author guqing
 * @date 2021-06-26
 */
public class FootnoteNodeRendererInterceptor {

    /**
     * Delegate the render method to intercept the FootNoteNodeRender by ByteBuddy runtime.
     */
    public static void doDelegationMethod() {
        ByteBuddyAgent.install();
        new ByteBuddy()
            .redefine(FootnoteNodeRenderer.class)

            .method(ElementMatchers.named("render").and(ElementMatchers.takesArguments(
                Footnote.class, NodeRendererContext.class, HtmlWriter.class)))
            .intercept(MethodDelegation.to(FootnoteNodeRendererInterceptor.class))

            .method(ElementMatchers.named("renderDocument"))
            .intercept(MethodDelegation.to(FootnoteNodeRendererInterceptor.class))

            .make()
            .load(Thread.currentThread().getContextClassLoader(),
                ClassReloadingStrategy.fromInstalledAgent());
    }

    /**
     * footnote render see {@link FootnoteNodeRenderer#renderDocument}.
     *
     * @param node footnote node
     * @param context node renderer context
     * @param html html writer
     */
    public static void render(Footnote node, NodeRendererContext context, HtmlWriter html) {
        FootnoteBlock footnoteBlock = node.getFootnoteBlock();
        if (footnoteBlock == null) {
            //just text
            html.raw("[^");
            context.renderChildren(node);
            html.raw("]");
        } else {
            int footnoteOrdinal = footnoteBlock.getFootnoteOrdinal();
            int i = node.getReferenceOrdinal();

            html.attr("class", "footnote-ref");
            html.srcPos(node.getChars()).withAttr()
                .tag("sup", false, false, () -> {
                    // if (!options.footnoteLinkRefClass.isEmpty()) html.attr("class", options
                    // .footnoteLinkRefClass);
                    String ordinal = footnoteOrdinal + (i == 0 ? "" : String.format(Locale.US,
                        ":%d", i));
                    html.attr("id", "fnref"
                        + ordinal);
                    html.attr("href", "#fn" + footnoteOrdinal);
                    html.withAttr().tag("a");
                    html.raw("[" + ordinal + "]");
                    html.tag("/a");
                });
        }
    }

    /**
     * render document.
     *
     * @param footnoteRepository footnoteRepository field of FootNoteRenderer class
     * @param options options field of FootNoteRenderer class
     * @param recheckUndefinedReferences recheckUndefinedReferences field of FootNoteRenderer class
     * @param context node render context
     * @param html html writer
     * @param document document
     * @param phase rendering phase
     */
    public static void renderDocument(@FieldValue("footnoteRepository")
        FootnoteRepository footnoteRepository,
        @FieldValue("options") FootnoteOptions options,
        @FieldValue("recheckUndefinedReferences")
            boolean recheckUndefinedReferences,
        @Argument(0) NodeRendererContext context,
        @Argument(1) HtmlWriter html, @Argument(2) Document document,
        @Argument(3)
            RenderingPhase phase) {
        final String footnoteBackLinkRefClass =
            (String) getFootnoteOptionsFieldValue("footnoteBackLinkRefClass", options);
        final String footnoteBackRefString = ObjectUtils
            .getDisplayString(getFootnoteOptionsFieldValue("footnoteBackRefString", options));

        if (phase == RenderingPhase.BODY_TOP) {
            if (recheckUndefinedReferences) {
                // need to see if have undefined footnotes that were defined after parsing
                boolean[] hadNewFootnotes = {false};
                NodeVisitor visitor = new NodeVisitor(
                    new VisitHandler<>(Footnote.class, node -> {
                        if (!node.isDefined()) {
                            FootnoteBlock footonoteBlock =
                                node.getFootnoteBlock(footnoteRepository);

                            if (footonoteBlock != null) {
                                footnoteRepository.addFootnoteReference(footonoteBlock, node);
                                node.setFootnoteBlock(footonoteBlock);
                                hadNewFootnotes[0] = true;
                            }
                        }
                    })
                );

                visitor.visit(document);
                if (hadNewFootnotes[0]) {
                    footnoteRepository.resolveFootnoteOrdinals();
                }
            }
        }

        if (phase == RenderingPhase.BODY_BOTTOM) {
            // here we dump the footnote blocks that were referenced in the document body, ie.
            // ones with footnoteOrdinal > 0
            if (footnoteRepository.getReferencedFootnoteBlocks().size() > 0) {
                html.attr("class", "footnotes-sep").withAttr().tagVoid("hr");
                html.attr("class", "footnotes").withAttr().tagIndent("section", () -> {
                    html.attr("class", "footnotes-list").withAttr().tagIndent("ol", () -> {
                        for (FootnoteBlock footnoteBlock : footnoteRepository
                            .getReferencedFootnoteBlocks()) {
                            int footnoteOrdinal = footnoteBlock.getFootnoteOrdinal();
                            html.attr("id", "fn" + footnoteOrdinal)
                                .attr("class", "footnote-item");
                            html.withAttr().tagIndent("li", () -> {
                                context.renderChildren(footnoteBlock);
                                int lineIndex = html.getLineCount() - 1;
                                BasedSequence line = html.getLine(lineIndex);
                                if (line.lastIndexOf("</p>") > -1) {
                                    int iMax = footnoteBlock.getFootnoteReferences();
                                    for (int i = 0; i < iMax; i++) {
                                        StringBuilder sb = new StringBuilder();
                                        sb.append(" <a href=\"#fnref").append(footnoteOrdinal)
                                            .append(i == 0 ? "" : String
                                                .format(Locale.US, ":%d", i)).append("\"");
                                        if (StringUtils.isNotBlank(footnoteBackLinkRefClass)) {
                                            sb.append(" class=\"").append(footnoteBackLinkRefClass)
                                                .append("\"");
                                        }
                                        sb.append(">").append(footnoteBackRefString).append("</a>");
                                        html.setLine(html.getLineCount() - 1, "",
                                            line.insert(line.lastIndexOf("</p"), sb.toString()));
                                    }
                                } else {
                                    int iMax = footnoteBlock.getFootnoteReferences();
                                    for (int i = 0; i < iMax; i++) {
                                        html.attr("href", "#fnref" + footnoteOrdinal
                                            + (i == 0 ? "" : String.format(Locale.US, ":%d", i)));
                                        if (StringUtils.isNotBlank(footnoteBackLinkRefClass)) {
                                            html.attr("class", footnoteBackLinkRefClass);
                                        }
                                        html.line().withAttr().tag("a");
                                        html.raw(footnoteBackRefString);
                                        html.tag("/a");
                                    }
                                }
                            });
                        }
                    });
                });
            }
        }
    }

    /**
     * Gets field value from FootnoteOptions.
     *
     * @param fieldName field name of FootNoteOptions class, must not be null.
     * @param options target object, must not be null.
     * @return field value.
     */
    private static Object getFootnoteOptionsFieldValue(String fieldName, FootnoteOptions options) {
        Assert.notNull(fieldName, "FieldName must not be null");
        Assert.notNull(options, "FootnoteOptions type must not be null");

        Object value = null;
        try {
            Field field = FootnoteOptions.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            value = field.get(options);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return value;
    }
}
