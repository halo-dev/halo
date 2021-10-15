package run.halo.app.utils.footnotes.internal;

import com.vladsch.flexmark.parser.block.AbstractBlockParser;
import com.vladsch.flexmark.parser.block.AbstractBlockParserFactory;
import com.vladsch.flexmark.parser.block.BlockContinue;
import com.vladsch.flexmark.parser.block.BlockParser;
import com.vladsch.flexmark.parser.block.BlockParserFactory;
import com.vladsch.flexmark.parser.block.BlockStart;
import com.vladsch.flexmark.parser.block.CustomBlockParserFactory;
import com.vladsch.flexmark.parser.block.MatchedBlockParser;
import com.vladsch.flexmark.parser.block.ParserState;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.BlockContent;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import run.halo.app.utils.footnotes.FootnoteBlock;
import run.halo.app.utils.footnotes.FootnoteExtension;

public class FootnoteBlockParser extends AbstractBlockParser {

    static String FOOTNOTE_ID = ".*";
    static Pattern FOOTNOTE_ID_PATTERN = Pattern.compile("\\[\\^\\s*(" + FOOTNOTE_ID + ")\\s*\\]");
    static Pattern FOOTNOTE_DEF_PATTERN =
        Pattern.compile("^\\[\\^\\s*(" + FOOTNOTE_ID + ")\\s*\\]:");

    private final FootnoteBlock block = new FootnoteBlock();
    private final FootnoteOptions options;
    private final int contentOffset;
    private BlockContent content = new BlockContent();

    public FootnoteBlockParser(FootnoteOptions options, int contentOffset) {
        this.options = options;
        this.contentOffset = contentOffset;
    }

    @Override
    public BlockContent getBlockContent() {
        return content;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        final int nonSpaceIndex = state.getNextNonSpaceIndex();
        if (state.isBlank()) {
            if (block.getFirstChild() == null) {
                // Blank line after empty list item
                return BlockContinue.none();
            } else {
                return BlockContinue.atIndex(nonSpaceIndex);
            }
        }

        if (state.getIndent() >= options.contentIndent) {
            int contentIndent = state.getIndex() + options.contentIndent;
            return BlockContinue.atIndex(contentIndent);
        } else {
            return BlockContinue.none();
        }
    }

    @Override
    public void addLine(ParserState state, BasedSequence line) {
        content.add(line, state.getIndent());
    }

    @Override
    public void closeBlock(ParserState state) {
        // set the footnote from closingMarker to end
        block.setCharsFromContent();
        block.setFootnote(block.getChars()
            .subSequence(block.getClosingMarker().getEndOffset() - block.getStartOffset())
            .trimStart());
        // add it to the map
        FootnoteRepository footnoteMap = FootnoteExtension.FOOTNOTES.get(state.getProperties());
        footnoteMap.put(footnoteMap.normalizeKey(block.getText()), block);
        content = null;
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    @Override
    public boolean canContain(ParserState state, BlockParser blockParser, Block block) {
        return true;
    }

    public static class Factory implements CustomBlockParserFactory {

        @Nullable
        @Override
        public Set<Class<?>> getAfterDependents() {
            return null;
        }

        @Nullable
        @Override
        public Set<Class<?>> getBeforeDependents() {
            return null;
        }

        @Override
        public boolean affectsGlobalScope() {
            return false;
        }

        @NonNull
        @Override
        public BlockParserFactory apply(@NonNull DataHolder options) {
            return new BlockFactory(options);
        }
    }

    private static class BlockFactory extends AbstractBlockParserFactory {

        private final FootnoteOptions options;

        private BlockFactory(DataHolder options) {
            super(options);
            this.options = new FootnoteOptions(options);
        }

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            if (state.getIndent() >= 4) {
                return BlockStart.none();
            }

            BasedSequence line = state.getLine();
            int nextNonSpace = state.getNextNonSpaceIndex();

            BasedSequence trySequence = line.subSequence(nextNonSpace, line.length());
            Matcher matcher = FOOTNOTE_DEF_PATTERN.matcher(trySequence);
            if (matcher.find()) {
                // abbreviation definition
                int openingStart = nextNonSpace + matcher.start();
                int openingEnd = nextNonSpace + matcher.end();
                BasedSequence openingMarker = line.subSequence(openingStart, openingStart + 2);
                BasedSequence text = line.subSequence(openingStart + 2, openingEnd - 2).trim();
                BasedSequence closingMarker = line.subSequence(openingEnd - 2, openingEnd);

                int contentOffset = options.contentIndent;

                FootnoteBlockParser footnoteBlockParser =
                    new FootnoteBlockParser(options, contentOffset);
                footnoteBlockParser.block.setOpeningMarker(openingMarker);
                footnoteBlockParser.block.setText(text);
                footnoteBlockParser.block.setClosingMarker(closingMarker);

                return BlockStart.of(footnoteBlockParser)
                    .atIndex(openingEnd);
            } else {
                return BlockStart.none();
            }
        }
    }
}
