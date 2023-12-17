package run.halo.app.content;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.ChangeDelta;
import com.github.difflib.patch.Chunk;
import com.github.difflib.patch.DeleteDelta;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.InsertDelta;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;
import com.google.common.base.Splitter;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import run.halo.app.infra.utils.JsonUtils;

/**
 * @author guqing
 * @since 2.0.0
 */
public class PatchUtils {
    private static final String DELIMITER = "\n";
    private static final Splitter lineSplitter = Splitter.on(DELIMITER);

    public static Patch<String> create(String deltasJson) {
        List<Delta> deltas = JsonUtils.jsonToObject(deltasJson, new TypeReference<>() {
        });
        Patch<String> patch = new Patch<>();
        for (Delta delta : deltas) {
            StringChunk sourceChunk = delta.getSource();
            StringChunk targetChunk = delta.getTarget();
            Chunk<String> orgChunk = new Chunk<>(sourceChunk.getPosition(), sourceChunk.getLines(),
                sourceChunk.getChangePosition());
            Chunk<String> revChunk = new Chunk<>(targetChunk.getPosition(), targetChunk.getLines(),
                targetChunk.getChangePosition());
            switch (delta.getType()) {
                case DELETE -> patch.addDelta(new DeleteDelta<>(orgChunk, revChunk));
                case INSERT -> patch.addDelta(new InsertDelta<>(orgChunk, revChunk));
                case CHANGE -> patch.addDelta(new ChangeDelta<>(orgChunk, revChunk));
                default -> throw new IllegalArgumentException("Unsupported delta type.");
            }
        }
        return patch;
    }

    public static String patchToJson(Patch<String> patch) {
        List<AbstractDelta<String>> deltas = patch.getDeltas();
        return JsonUtils.objectToJson(deltas);
    }

    public static String applyPatch(String original, String patchJson) {
        Patch<String> patch = PatchUtils.create(patchJson);
        try {
            return String.join(DELIMITER, patch.applyTo(breakLine(original)));
        } catch (PatchFailedException e) {
            throw new RuntimeException(e);
        }
    }

    public static String diffToJsonPatch(String original, String revised) {
        Patch<String> patch = DiffUtils.diff(breakLine(original), breakLine(revised));
        return PatchUtils.patchToJson(patch);
    }

    public static List<String> breakLine(String content) {
        if (StringUtils.isBlank(content)) {
            return Collections.emptyList();
        }
        return lineSplitter.splitToList(content);
    }

    @Data
    public static class Delta {
        private StringChunk source;
        private StringChunk target;
        private DeltaType type;
    }

    @Data
    public static class StringChunk {
        private int position;
        private List<String> lines;
        private List<Integer> changePosition;
    }
}
