package run.halo.app.utils.flexmark.ext.math.internal;

import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.data.DataHolder;
import org.jetbrains.annotations.NotNull;
import run.halo.app.utils.flexmark.ext.math.MathBlock;
import run.halo.app.utils.flexmark.ext.math.MathInline;

import java.util.HashSet;
import java.util.Set;

public class MathNodeRenderer implements NodeRenderer {
    final MathOptions mOptions;

    public MathNodeRenderer(DataHolder options) {
        this.mOptions = new MathOptions(options);
    }

    public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        Set<NodeRenderingHandler<?>> set = new HashSet<>();
        set.add(new NodeRenderingHandler<>(MathInline.class, this::render));
        set.add(new NodeRenderingHandler<>(MathBlock.class, this::render));
        return set;
    }

    private void render(MathInline node, NodeRendererContext content, HtmlWriter html) {
        ((HtmlWriter)html.withAttr().attr("class", this.mOptions.inlineMathClass)).withAttr().tag("span");
        html.text((CharSequence)node.getText());
        html.tag("/span");
    }

    private void render(MathBlock node, NodeRendererContext content, HtmlWriter html) {
        ((HtmlWriter)html.withAttr().attr("class", this.mOptions.blockMathClass)).withAttr().tag("div");
        html.text((CharSequence)node.getOpeningTrailing());
        html.text((CharSequence)node.getChildChars());
        html.text((CharSequence)node.getClosingTrailing());
        html.tag("/div");
    }

    public static class Factory implements NodeRendererFactory {
        @Override
        public @NotNull NodeRenderer apply(@NotNull DataHolder options) {
            return new MathNodeRenderer(options);
        }
    }
}
