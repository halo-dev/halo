package run.halo.app.utils.flexmark.ext.math;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.formatter.NodeFormatterFactory;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.DataKey;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import org.jetbrains.annotations.NotNull;
import run.halo.app.utils.flexmark.ext.math.internal.MathBlockParser;
import run.halo.app.utils.flexmark.ext.math.internal.MathDelimiterProcessor;
import run.halo.app.utils.flexmark.ext.math.internal.MathNodeFormatter;
import run.halo.app.utils.flexmark.ext.math.internal.MathNodeRenderer;

public class MathExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension, Formatter.FormatterExtension {
    public static final DataKey<String> INLINE_MATH_CLASS = new DataKey<>("INLINE_MATH_CLASS", "math");

    public static final DataKey<String> BLOCK_MATH_CLASS = new DataKey<>("BLOCK_MATH_CLASS", "math");

    public static final DataKey<Boolean> NESTED_BLOCK_MATH = new DataKey<>("BLOCK_MATH_NESTED",true);

    public static MathExtension create() {
        return new MathExtension();
    }

    public void parserOptions(MutableDataHolder mutableDataHolder) {}

    public void rendererOptions(@NotNull MutableDataHolder mutableDataHolder) {}

    public void extend(Parser.Builder builder) {
        builder.customDelimiterProcessor(new MathDelimiterProcessor());
        builder.customBlockParserFactory(new MathBlockParser.Factory());
    }

    public void extend(HtmlRenderer.Builder builder, String s) {
        if (builder.isRendererType("HTML")) {
            builder.nodeRendererFactory((NodeRendererFactory)new MathNodeRenderer.Factory());
        } else if (builder.isRendererType("JIRA")) {

        }
    }

    public void extend(Formatter.Builder builder) {
        builder.nodeFormatterFactory((NodeFormatterFactory)new MathNodeFormatter.Factory());
    }
}
