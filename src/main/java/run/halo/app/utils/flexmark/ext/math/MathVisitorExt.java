package run.halo.app.utils.flexmark.ext.math;

import com.vladsch.flexmark.util.ast.VisitHandler;

public class MathVisitorExt {
    public static <V extends MathVisitor> VisitHandler<?>[] VISIT_HANDLERS(V visitor) {
        return new VisitHandler<?>[] {
                new VisitHandler<>(MathInline.class, visitor::visit),
                new VisitHandler<>(MathBlock.class, visitor::visit)
        };
    }
}