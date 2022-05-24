package run.halo.app.extension;

import lombok.Data;

/**
 * AbstractExtension contains basic structure of Extension and implements the Extension interface.
 *
 * @author johnniang
 */
@Data
public abstract class AbstractExtension implements Extension {

    private String apiVersion;

    private String kind;

    private MetadataOperator metadata;

    @Override
    public void groupVersionKind(GroupVersionKind gvk) {
        this.apiVersion = gvk.groupVersion().toString();
        this.kind = gvk.kind();
    }

    @Override
    public GroupVersionKind groupVersionKind() {
        return GroupVersionKind.fromAPIVersionAndKind(this.apiVersion, this.kind);
    }

    public MetadataOperator getMetadata() {
        return metadata;
    }

    @Override
    public void setMetadata(MetadataOperator metadata) {
        this.metadata = metadata;
    }
}
