package run.halo.app.utils.flexmark.ext.math.internal;

import com.vladsch.flexmark.formatter.*;
import com.vladsch.flexmark.util.data.DataHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import run.halo.app.utils.flexmark.ext.math.MathBlock;
import run.halo.app.utils.flexmark.ext.math.MathInline;

import java.util.HashSet;
import java.util.Set;

public class MathNodeFormatter implements NodeFormatter {
    public MathNodeFormatter(DataHolder options) {}

    @Nullable
    @Override
    public Set<NodeFormattingHandler<?>> getNodeFormattingHandlers() {
        Set<NodeFormattingHandler<?>> set = new HashSet<>();
        set.add(new NodeFormattingHandler<>(MathInline.class, this::render));
        set.add(new NodeFormattingHandler<>(MathBlock.class, this::render));
        return set;
    }

    private void render(MathInline node, NodeFormatterContext context, MarkdownWriter markdown) {
        markdown.append(node.getOpeningMarker());
        markdown.appendNonTranslating(node.getText());
        markdown.append(node.getClosingMarker());
    }

    private void render(MathBlock node, NodeFormatterContext context, MarkdownWriter markdown) {
    }

    @Nullable
    @Override
    public Set<Class<?>> getNodeClasses() {
        return null;
    }

    public static class Factory implements NodeFormatterFactory {
        @NotNull
        @Override
        public NodeFormatter create(@NotNull DataHolder options) {
            return new MathNodeFormatter(options);
        }
    }
}
