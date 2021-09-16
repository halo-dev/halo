package run.halo.app.utils.footnotes;

import com.vladsch.flexmark.ast.LinkRendered;
import com.vladsch.flexmark.util.ast.DelimitedNode;
import com.vladsch.flexmark.util.ast.DoNotDecorate;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.ReferencingNode;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.springframework.lang.NonNull;
import run.halo.app.utils.footnotes.internal.FootnoteRepository;

/**
 * A Footnote referencing node
 */
public class Footnote extends Node implements DelimitedNode, DoNotDecorate, LinkRendered,
    ReferencingNode<FootnoteRepository, FootnoteBlock> {
    protected BasedSequence openingMarker = BasedSequence.NULL;
    protected BasedSequence text = BasedSequence.NULL;
    protected BasedSequence closingMarker = BasedSequence.NULL;
    protected FootnoteBlock footnoteBlock;

    public int getReferenceOrdinal() {
        return referenceOrdinal;
    }

    public void setReferenceOrdinal(int referenceOrdinal) {
        this.referenceOrdinal = referenceOrdinal;
    }

    protected int referenceOrdinal;

    @NonNull
    @Override
    public BasedSequence getReference() {
        return text;
    }

    @Override
    public FootnoteBlock getReferenceNode(Document document) {
        if (footnoteBlock != null || text.isEmpty()) {
            return footnoteBlock;
        }
        footnoteBlock = getFootnoteBlock(FootnoteExtension.FOOTNOTES.get(document));
        return footnoteBlock;
    }

    @Override
    public FootnoteBlock getReferenceNode(FootnoteRepository repository) {
        if (footnoteBlock != null || text.isEmpty()) {
            return footnoteBlock;
        }
        footnoteBlock = getFootnoteBlock(repository);
        return footnoteBlock;
    }

    @Override
    public boolean isDefined() {
        return footnoteBlock != null;
    }

    /**
     * @return true if this node will be rendered as text because it depends on a reference which
     * is not defined.
     */
    @Override
    public boolean isTentative() {
        return footnoteBlock == null;
    }

    public FootnoteBlock getFootnoteBlock(FootnoteRepository footnoteRepository) {
        return text.isEmpty() ? null : footnoteRepository.get(text.toString());
    }

    public FootnoteBlock getFootnoteBlock() {
        return footnoteBlock;
    }

    public void setFootnoteBlock(FootnoteBlock footnoteBlock) {
        this.footnoteBlock = footnoteBlock;
    }

    @NonNull
    @Override
    public BasedSequence[] getSegments() {
        return new BasedSequence[] {openingMarker, text, closingMarker};
    }

    @Override
    public void getAstExtra(@NonNull StringBuilder out) {
        out.append(" ordinal: ")
            .append(footnoteBlock != null ? footnoteBlock.getFootnoteOrdinal() : 0).append(" ");
        delimitedSegmentSpanChars(out, openingMarker, text, closingMarker, "text");
    }

    public Footnote() {
    }

    public Footnote(BasedSequence chars) {
        super(chars);
    }

    public Footnote(BasedSequence openingMarker, BasedSequence text, BasedSequence closingMarker) {
        super(openingMarker
            .baseSubSequence(openingMarker.getStartOffset(), closingMarker.getEndOffset()));
        this.openingMarker = openingMarker;
        this.text = text;
        this.closingMarker = closingMarker;
    }

    @Override
    public BasedSequence getOpeningMarker() {
        return openingMarker;
    }

    @Override
    public void setOpeningMarker(BasedSequence openingMarker) {
        this.openingMarker = openingMarker;
    }

    @Override
    public BasedSequence getText() {
        return text;
    }

    @Override
    public void setText(BasedSequence text) {
        this.text = text;
    }

    @Override
    public BasedSequence getClosingMarker() {
        return closingMarker;
    }

    @Override
    public void setClosingMarker(BasedSequence closingMarker) {
        this.closingMarker = closingMarker;
    }
}
