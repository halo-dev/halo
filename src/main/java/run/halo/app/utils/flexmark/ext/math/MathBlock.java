package run.halo.app.utils.flexmark.ext.math;

import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.ParagraphContainer;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.BlockContent;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MathBlock extends Block implements ParagraphContainer {
    private BasedSequence openingMarker = BasedSequence.NULL;

    private BasedSequence openingTrailing = BasedSequence.NULL;

    private BasedSequence closingMarker = BasedSequence.NULL;

    private BasedSequence closingTrailing = BasedSequence.NULL;

    public MathBlock() {}

    public MathBlock(BasedSequence chars) {
        super(chars);
    }

    public MathBlock(BasedSequence chars, List<BasedSequence> segments) {
        super(chars, segments);
    }

    public MathBlock(BlockContent content) {
        super(content);
    }

    public BasedSequence getOpeningMarker() {
        return this.openingMarker;
    }

    public void setOpeningMarker(BasedSequence openingMarker) {
        this.openingMarker = openingMarker;
    }

    public BasedSequence getClosingMarker() {
        return this.closingMarker;
    }

    public void setClosingMarker(BasedSequence closingMarker) {
        this.closingMarker = closingMarker;
    }

    public BasedSequence getOpeningTrailing() {
        return this.openingTrailing;
    }

    public void setOpeningTrailing(BasedSequence openingTrailing) {
        this.openingTrailing = openingTrailing;
    }

    public BasedSequence getClosingTrailing() {
        return this.closingTrailing;
    }

    public void setClosingTrailing(BasedSequence closingTrailing) {
        this.closingTrailing = closingTrailing;
    }

    public void getAstExtra(@NotNull StringBuilder out) {
        segmentSpanChars(out, this.openingMarker, "open");
        segmentSpanChars(out, this.openingMarker, "openTrail");
        segmentSpanChars(out, this.closingMarker, "close");
        segmentSpanChars(out, this.openingMarker, "closeTail");
    }

    public boolean isParagraphEndWrappingDisabled(Paragraph paragraph) {
        return (paragraph == getLastChild() || paragraph.getNext() instanceof MathBlock);
    }

    public boolean isParagraphStartWrappingDisabled(Paragraph paragraph) {
        return (paragraph == getFirstChild() || paragraph.getPrevious() instanceof MathBlock);
    }

    public BasedSequence[] getSegments() {
        return new BasedSequence[] { this.openingMarker, this.openingTrailing, this.closingMarker, this.closingTrailing };
    }
}

