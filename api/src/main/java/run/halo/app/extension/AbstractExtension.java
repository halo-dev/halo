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
    public String getApiVersion() {
        var apiVersionFromGvk = Extension.super.getApiVersion();
        return apiVersionFromGvk != null ? apiVersionFromGvk : this.apiVersion;
    }

    @Override
    public String getKind() {
        var kindFromGvk = Extension.super.getKind();
        return kindFromGvk != null ? kindFromGvk : this.kind;
    }
}
