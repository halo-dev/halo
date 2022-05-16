package run.halo.app.extension;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AbstractExtension contains basic structure of Extension and implements the Extension interface.
 *
 * @author johnniang
 */
@Data
public class AbstractExtension implements Extension {

    @Schema(required = true)
    private String apiVersion;

    @Schema(required = true)
    private String kind;

    @Schema(required = true)
    private Metadata metadata;

    @Override
    public void groupVersionKind(GroupVersionKind gvk) {
        this.apiVersion = gvk.groupVersion().toString();
        this.kind = gvk.kind();
    }

    @Override
    public GroupVersionKind groupVersionKind() {
        return GroupVersionKind.fromAPIVersionAndKind(this.apiVersion, this.kind);
    }

    @Override
    public void metadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public Metadata metadata() {
        return this.metadata;
    }
}
