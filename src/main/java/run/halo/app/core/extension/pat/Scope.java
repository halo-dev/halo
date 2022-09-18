package run.halo.app.core.extension.pat;

import static run.halo.app.core.extension.pat.Constant.GROUP;
import static run.halo.app.core.extension.pat.Constant.VERSION;

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

    private String description;

    private String identity;

    private Set<String> include;

    private Set<String> dependsOn;
}
