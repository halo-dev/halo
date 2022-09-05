package run.halo.app.extension;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Extension reference object. The name is mandatory")
public class Ref {

    @Schema(description = "Extension group")
    private String group;

    @Schema(description = "Extension version")
    private String version;

    @Schema(description = "Extension kind")
    private String kind;

    @Schema(required = true, description = "Extension name. This field is mandatory")
    private String name;

    public static Ref of(String name) {
        Ref ref = new Ref();
        ref.setName(name);
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
}
