package run.halo.app.utils.flexmark.ext.math.internal;

import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.core.delimiter.Delimiter;
import com.vladsch.flexmark.parser.delimiter.DelimiterProcessor;
import com.vladsch.flexmark.parser.delimiter.DelimiterRun;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import run.halo.app.utils.flexmark.ext.math.MathInline;

public class MathDelimiterProcessor implements DelimiterProcessor {
    public char getOpeningCharacter() {
        return '$';
    }

    public char getClosingCharacter() {
        return '$';
    }

    public int getMinLength() {
        return 1;
    }

    public int getDelimiterUse(DelimiterRun opener, DelimiterRun closer) {
        if (opener.length() >= 1 && closer.length() >= 1)
            return 1;
        return 0;
    }

    public void process(Delimiter opener, Delimiter closer, int delimitersUsed) {
        MathInline mathInline = new MathInline(opener.getTailChars(delimitersUsed), BasedSequence.NULL, closer.getLeadChars(delimitersUsed));
        opener.moveNodesBetweenDelimitersTo(mathInline, closer);
    }

    public Node unmatchedDelimiterNode(InlineParser inlineParser, DelimiterRun delimiterRun) {
        return null;
    }

    public boolean canBeOpener(String before, String after, boolean leftFlanking, boolean rightFlanking, boolean beforeIsPunctuation, boolean afterIsPunctuation, boolean beforeIsWhitespace, boolean afterIsWhiteSpace) {
        return leftFlanking;
    }

    public boolean canBeCloser(String before, String after, boolean leftFlanking, boolean rightFlanking, boolean beforeIsPunctuation, boolean afterIsPunctuation, boolean beforeIsWhitespace, boolean afterIsWhiteSpace) {
        return rightFlanking;
    }

    public boolean skipNonOpenerCloser() {
        return false;
    }
}
