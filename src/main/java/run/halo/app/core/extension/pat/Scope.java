package run.halo.app.core.extension.pat;

import static run.halo.app.core.extension.pat.Constant.GROUP;
import static run.halo.app.core.extension.pat.Constant.VERSION;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@GVK(group = GROUP, version = VERSION, kind = "Scope",
    plural = "scopes", singular = "scope")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Scope extends AbstractExtension {

    @Schema(required = true, description = "The description of the scope")
    private String description;

    @Schema(required = true, description = "The unique identity of the scope")
    private String identity;

    @Schema(nullable = true, description = "Parent scope")
    private String parent;

    @Schema(description = "Dependent scopes")
    private Set<String> dependsOn;
}
