package run.halo.app.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * Content patch utilities.
 *
 * @author guqing
 * @date 2021-12-19
 */
public class PatchUtils {

    public static Patch<String> create(String deltasJson) {
        List<Delta> deltas = JsonUtils.jsonToObject(deltasJson, new TypeReference<>() {});
        Patch<String> patch = new Patch<>();
        for (Delta delta : deltas) {
            StringChunk sourceChunk = delta.getSource();
            StringChunk targetChunk = delta.getTarget();
            Chunk<String> orgChunk = new Chunk<>(sourceChunk.getPosition(), sourceChunk.getLines(),
                sourceChunk.getChangePosition());
            Chunk<String> revChunk = new Chunk<>(targetChunk.getPosition(), targetChunk.getLines(),
                targetChunk.getChangePosition());
            switch (delta.getType()) {
                case DELETE:
                    patch.addDelta(new DeleteDelta<>(orgChunk, revChunk));
                    break;
                case INSERT:
                    patch.addDelta(new InsertDelta<>(orgChunk, revChunk));
                    break;
                case CHANGE:
                    patch.addDelta(new ChangeDelta<>(orgChunk, revChunk));
                    break;
            }
        }
        return patch;
    }

    public static String patchToJson(Patch<String> patch) {
        List<AbstractDelta<String>> deltas = patch.getDeltas();
        try {
            return JsonUtils.objectToJson(deltas);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String restoreContent(String json, String original) {
        Patch<String> patch = PatchUtils.create(json);
        try {
            return String.join("\n", patch.applyTo(breakLine(original)));
        } catch (PatchFailedException e) {
            throw new RuntimeException(e);
        }
    }

    public static String diffToJsonPatch(String original, String revised) {
        Patch<String> patch = DiffUtils.diff(breakLine(original), breakLine(revised));
        return PatchUtils.patchToJson(patch);
    }

    public static List<String> breakLine(String content) {
        String[] strings = StringUtils.tokenizeToStringArray(content, "\n");
        return Arrays.asList(strings);
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
