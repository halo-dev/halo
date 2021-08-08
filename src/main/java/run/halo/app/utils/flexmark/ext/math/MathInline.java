package run.halo.app.utils.flexmark.ext.math;

import com.vladsch.flexmark.util.ast.DelimitedNode;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;

public class MathInline extends Node implements DelimitedNode {
    protected BasedSequence openingMarker = BasedSequence.NULL;

    protected BasedSequence text = BasedSequence.NULL;

    protected BasedSequence closingMarker = BasedSequence.NULL;

    public MathInline() {}

    public MathInline(BasedSequence chars) {
        super(chars);
    }

    public MathInline(BasedSequence openingMarker, BasedSequence text, BasedSequence closingMarker) {
        super(openingMarker.baseSubSequence(openingMarker.getStartOffset(), closingMarker.getEndOffset()));
        this.openingMarker = openingMarker;
        this.text = text;
        this.closingMarker = closingMarker;
    }

    public BasedSequence getOpeningMarker() {
        return this.openingMarker;
    }

    public void setOpeningMarker(BasedSequence basedSequence) {
        this.openingMarker = basedSequence;
    }

    public BasedSequence getText() {
        return this.text;
    }

    public void setText(BasedSequence basedSequence) {
        this.text = basedSequence;
    }

    public BasedSequence getClosingMarker() {
        return this.closingMarker;
    }

    public void setClosingMarker(BasedSequence basedSequence) {
        this.closingMarker = basedSequence;
    }

    public BasedSequence[] getSegments() {
        return new BasedSequence[] { this.openingMarker, this.text, this.closingMarker };
    }
}
