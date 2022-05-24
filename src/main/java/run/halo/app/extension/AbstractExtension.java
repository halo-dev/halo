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

}
