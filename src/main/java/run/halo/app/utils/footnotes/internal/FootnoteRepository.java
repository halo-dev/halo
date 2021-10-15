package run.halo.app.utils.footnotes.internal;

import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.KeepType;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeRepository;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.VisitHandler;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.DataKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.lang.NonNull;
import run.halo.app.utils.footnotes.Footnote;
import run.halo.app.utils.footnotes.FootnoteBlock;
import run.halo.app.utils.footnotes.FootnoteExtension;

@SuppressWarnings("WeakerAccess")
public class FootnoteRepository extends NodeRepository<FootnoteBlock> {

    private final ArrayList<FootnoteBlock> referencedFootnoteBlocks = new ArrayList<>();

    public static void resolveFootnotes(Document document) {
        FootnoteRepository footnoteRepository = FootnoteExtension.FOOTNOTES.get(document);

        boolean[] hadNewFootnotes = {false};
        NodeVisitor visitor = new NodeVisitor(
            new VisitHandler<>(Footnote.class, node -> {
                if (!node.isDefined()) {
                    FootnoteBlock footonoteBlock = node.getFootnoteBlock(footnoteRepository);

                    if (footonoteBlock != null) {
                        footnoteRepository.addFootnoteReference(footonoteBlock, node);
                        node.setFootnoteBlock(footonoteBlock);
                        hadNewFootnotes[0] = true;
                    }
                }
            })
        );

        visitor.visit(document);
        if (hadNewFootnotes[0]) {
            footnoteRepository.resolveFootnoteOrdinals();
        }
    }

    public void addFootnoteReference(FootnoteBlock footnoteBlock, Footnote footnote) {
        if (!footnoteBlock.isReferenced()) {
            referencedFootnoteBlocks.add(footnoteBlock);
        }

        footnoteBlock.setFirstReferenceOffset(footnote.getStartOffset());

        int referenceOrdinal = footnoteBlock.getFootnoteReferences();
        footnoteBlock.setFootnoteReferences(referenceOrdinal + 1);
        footnote.setReferenceOrdinal(referenceOrdinal);
    }

    public void resolveFootnoteOrdinals() {
        // need to sort by first referenced offset then set each to its ordinal position in the
        // array+1
        Collections.sort(referencedFootnoteBlocks,
            (f1, f2) -> f1.getFirstReferenceOffset() - f2.getFirstReferenceOffset());

        int ordinal = 0;
        for (FootnoteBlock footnoteBlock : referencedFootnoteBlocks) {
            footnoteBlock.setFootnoteOrdinal(++ordinal);
        }
    }

    public List<FootnoteBlock> getReferencedFootnoteBlocks() {
        return referencedFootnoteBlocks;
    }

    public FootnoteRepository(DataHolder options) {
        super(FootnoteExtension.FOOTNOTES_KEEP.get(options));
    }

    @NonNull
    @Override
    public DataKey<FootnoteRepository> getDataKey() {
        return FootnoteExtension.FOOTNOTES;
    }

    @NonNull
    @Override
    public DataKey<KeepType> getKeepDataKey() {
        return FootnoteExtension.FOOTNOTES_KEEP;
    }

    @NonNull
    @Override
    public Set<FootnoteBlock> getReferencedElements(Node parent) {
        HashSet<FootnoteBlock> references = new HashSet<>();
        visitNodes(parent, value -> {
            if (value instanceof Footnote) {
                FootnoteBlock reference =
                    ((Footnote) value).getReferenceNode(FootnoteRepository.this);
                if (reference != null) {
                    references.add(reference);
                }
            }
        }, Footnote.class);
        return references;
    }
}
