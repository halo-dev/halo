package run.halo.app.utils.footnotes.internal;

import com.vladsch.flexmark.formatter.MarkdownWriter;
import com.vladsch.flexmark.formatter.NodeFormatter;
import com.vladsch.flexmark.formatter.NodeFormatterContext;
import com.vladsch.flexmark.formatter.NodeFormatterFactory;
import com.vladsch.flexmark.formatter.NodeFormattingHandler;
import com.vladsch.flexmark.formatter.NodeRepositoryFormatter;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.DataKey;
import com.vladsch.flexmark.util.format.options.ElementPlacement;
import com.vladsch.flexmark.util.format.options.ElementPlacementSort;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import run.halo.app.utils.footnotes.Footnote;
import run.halo.app.utils.footnotes.FootnoteBlock;
import run.halo.app.utils.footnotes.FootnoteExtension;

public class FootnoteNodeFormatter
    extends NodeRepositoryFormatter<FootnoteRepository, FootnoteBlock, Footnote> {

    public static final DataKey<Map<String, String>> FOOTNOTE_TRANSLATION_MAP =
        new DataKey<>("FOOTNOTE_TRANSLATION_MAP", new HashMap<>()); // translated references
    public static final DataKey<Map<String, String>> FOOTNOTE_UNIQUIFICATION_MAP =
        new DataKey<>("FOOTNOTE_UNIQUIFICATION_MAP", new HashMap<>()); // uniquified references
    private final FootnoteFormatOptions options;

    public FootnoteNodeFormatter(DataHolder options) {
        super(options, FOOTNOTE_TRANSLATION_MAP, FOOTNOTE_UNIQUIFICATION_MAP);
        this.options = new FootnoteFormatOptions(options);
    }

    @Override
    public FootnoteRepository getRepository(DataHolder options) {
        return FootnoteExtension.FOOTNOTES.get(options);
    }

    @Override
    public ElementPlacement getReferencePlacement() {
        return options.footnotePlacement;
    }

    @Override
    public ElementPlacementSort getReferenceSort() {
        return options.footnoteSort;
    }

    @Override
    public void renderReferenceBlock(FootnoteBlock node, NodeFormatterContext context,
        MarkdownWriter markdown) {
        markdown.blankLine().append("[^");
        markdown.append(transformReferenceId(node.getText().toString(), context));
        markdown.append("]: ");
        markdown.pushPrefix().addPrefix("    ");
        context.renderChildren(node);
        markdown.popPrefix();
        markdown.blankLine();
    }

    @Nullable
    @Override
    public Set<NodeFormattingHandler<?>> getNodeFormattingHandlers() {
        return new HashSet<>(Arrays.asList(
            new NodeFormattingHandler<>(Footnote.class, FootnoteNodeFormatter.this::render),
            new NodeFormattingHandler<>(FootnoteBlock.class, FootnoteNodeFormatter.this::render)
        ));
    }

    @Nullable
    @Override
    public Set<Class<?>> getNodeClasses() {
        if (options.footnotePlacement.isNoChange() || !options.footnoteSort.isUnused()) {
            return null;
        }
        // noinspection ArraysAsListWithZeroOrOneArgument
        return new HashSet<>(Arrays.asList(
            Footnote.class
        ));
    }

    private void render(FootnoteBlock node, NodeFormatterContext context, MarkdownWriter markdown) {
        renderReference(node, context, markdown);
    }

    private void render(Footnote node, NodeFormatterContext context, MarkdownWriter markdown) {
        markdown.append("[^");
        if (context.isTransformingText()) {
            String referenceId = transformReferenceId(node.getText().toString(), context);
            context.nonTranslatingSpan((context1, markdown1) -> markdown1.append(referenceId));
        } else {
            markdown.append(node.getText());
        }
        markdown.append("]");
    }

    public static class Factory implements NodeFormatterFactory {

        @NonNull
        @Override
        public NodeFormatter create(@NonNull DataHolder options) {
            return new FootnoteNodeFormatter(options);
        }
    }
}
