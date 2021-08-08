package run.halo.app.utils.flexmark.ext.math;

public interface MathVisitor {
    void visit(MathInline paramMathInline);

    void visit(MathBlock paramMathBlock);
}
