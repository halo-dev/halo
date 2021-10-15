package run.halo.app.utils.footnotes;

import com.vladsch.flexmark.util.ast.VisitHandler;

public class FootnoteVisitorExt {

    public static <V extends FootnoteVisitor> VisitHandler<?>[] visitHandlers(V visitor) {
        return new VisitHandler<?>[] {
            new VisitHandler<>(FootnoteBlock.class, visitor::visit),
            new VisitHandler<>(Footnote.class, visitor::visit),
        };
    }
}
