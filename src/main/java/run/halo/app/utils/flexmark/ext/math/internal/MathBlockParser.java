package run.halo.app.utils.flexmark.ext.math.internal;

import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.block.*;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.BlockContent;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import run.halo.app.utils.flexmark.ext.math.MathBlock;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MathBlockParser extends AbstractBlockParser {
    static Pattern MATH_BLOCK_START = Pattern.compile("^ {0,3}\\${2}(.*\n$)");

    static Pattern MATH_BLOCK_END = Pattern.compile("(.*?)\\${2}\\s*\n?$");

    private final MathBlock mBlock = new MathBlock();

    private BlockContent mContent = new BlockContent();

    private final MathOptions mOptions;

    private boolean hadClose = false;

    MathBlockParser(DataHolder options, BasedSequence openMarker, BasedSequence openTrailing) {
        this.mOptions = new MathOptions(options);
        this.mBlock.setOpeningMarker(openMarker);
        this.mBlock.setOpeningTrailing(openTrailing);
    }

    public Block getBlock() {
        return (Block)this.mBlock;
    }

    public BlockContinue tryContinue(ParserState state) {
        if (this.hadClose)
            return BlockContinue.none();
        int index = state.getIndex();
        BasedSequence line = state.getLineWithEOL();
        Matcher matcher = MATH_BLOCK_END.matcher((CharSequence)line.subSequence(index));
        if (!matcher.matches())
            return BlockContinue.atIndex(index);
        Node lastChild = this.mBlock.getLastChild();
        if (lastChild instanceof MathBlock) {
            BlockParser parser = state.getActiveBlockParser((Block)lastChild);
            if (parser instanceof MathBlockParser && !((MathBlockParser)parser).hadClose)
                return BlockContinue.atIndex(index);
        }
        this.hadClose = true;
        this.mBlock.setClosingMarker(state.getLine().subSequence(index, index + 2));
        this.mBlock.setClosingTrailing(state.getLineWithEOL().subSequence(matcher.start(1), matcher.end(1)));
        return BlockContinue.atIndex(state.getLineEndIndex());
    }

    public void addLine(ParserState state, BasedSequence line) {
        this.mContent.add(line, state.getIndent());
    }

    public void closeBlock(ParserState parserState) {
        this.mBlock.setContent(this.mContent);
        this.mBlock.setCharsFromContent();
        this.mContent = null;
    }

    public boolean isContainer() {
        return true;
    }

    public boolean canContain(ParserState state, BlockParser blockParser, Block block) {
        return true;
    }

    public void parseInlines(InlineParser inlineParser) {}

    public  static class Factory implements CustomBlockParserFactory {
        @Override
        public @NotNull BlockParserFactory apply(@NotNull DataHolder options) {
            return new BlockFactory(options);
        }

        @Override
        public @Nullable Set<Class<?>> getAfterDependents() {
            return null;
        }

        @Override
        public @Nullable Set<Class<?>> getBeforeDependents() {
            return null;
        }

        @Override
        public boolean affectsGlobalScope() {
            return false;
        }
    }

    private static class BlockFactory extends AbstractBlockParserFactory {
        final private MathOptions options;

        public BlockFactory(DataHolder options) {
            super(options);
            this.options = new MathOptions(options);
        }

        boolean haveMathBlockParser(ParserState state) {
            List<BlockParser> parsers = state.getActiveBlockParsers();
            int i = parsers.size();
            while(i-- > 0) {
                if (parsers.get(i) instanceof MathBlockParser) return true;
            }
            return false;
        }

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            if (options.nestedBlockMath || !haveMathBlockParser(state)) {
                BasedSequence line = state.getLineWithEOL();
                Matcher matcher = MATH_BLOCK_START.matcher(line);
                if (matcher.matches()) {
                    return BlockStart.of(
                            new MathBlockParser(
                                    state.getProperties(),
                                    line.subSequence(0, 2),
                                    line.subSequence(matcher.start(1), matcher.end(1))
                            )).atIndex(state.getLineEndIndex());
                }
            }
            return BlockStart.none();
        }
    }
}