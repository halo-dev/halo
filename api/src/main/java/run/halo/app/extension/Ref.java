package run.halo.app.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import lombok.Data;
import org.springframework.lang.NonNull;

@Data
@Schema(description = "Extension reference object. The name is mandatory")
public class Ref {

    @Schema(description = "Extension group")
    private String group;

    @Schema(description = "Extension version")
    private String version;

    @Schema(description = "Extension kind")
    private String kind;

    @Schema(requiredMode = REQUIRED, description = "Extension name. This field is mandatory")
    private String name;

    public static Ref of(String name) {
        Ref ref = new Ref();
        ref.setName(name);
        return ref;
    }

    public static Ref of(String name, GroupVersionKind gvk) {
        Ref ref = new Ref();
        ref.setName(name);
        ref.setGroup(gvk.group());
        ref.setVersion(gvk.version());
        ref.setKind(gvk.kind());
        return ref;
    }

    public static Ref of(Extension extension) {
        var metadata = extension.getMetadata();
        var gvk = extension.groupVersionKind();
        var ref = new Ref();
        ref.setName(metadata.getName());
        ref.setGroup(gvk.group());
        ref.setVersion(gvk.version());
        ref.setKind(gvk.kind());
        return ref;
    }

    /**
     * Check the ref has the same group and kind.
     *
     * @param ref is target reference
     * @param gvk is group version kind
     * @return true if they have the same group and kind.
     */
    public static boolean groupKindEquals(Ref ref, GroupVersionKind gvk) {
        return Objects.equals(ref.getGroup(), gvk.group())
            && Objects.equals(ref.getKind(), gvk.kind());
    }

    /**
     * Check if the extension is equal to the ref.
     *
     * @param ref must not be null.
     * @param extension must not be null.
     * @return true if they are equal; false otherwise.
     */
    public static boolean equals(@NonNull Ref ref, @NonNull ExtensionOperator extension) {
        var gvk = extension.groupVersionKind();
        var name = extension.getMetadata().getName();
        return groupKindEquals(ref, gvk) && Objects.equals(ref.getName(), name);
    }

}
