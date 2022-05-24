package run.halo.app.extension;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * ExtensionOperator contains some getters and setters for required fields of Extension.
 *
 * @author johnniang
 */
public interface ExtensionOperator {

    @Schema(required = true)
    @JsonProperty("apiVersion")
    String getApiVersion();

    @Schema(required = true)
    @JsonProperty("kind")
    String getKind();

    @Schema(required = true, implementation = Metadata.class)
    @JsonProperty("metadata")
    MetadataOperator getMetadata();

    void setApiVersion(String apiVersion);

    void setKind(String kind);

    void setMetadata(MetadataOperator metadata);

}
