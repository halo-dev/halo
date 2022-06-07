package run.halo.app.security.authorization;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * @author guqing
 * @since 2.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@GVK(group = "", version = "v1alpha1", kind = "Role", plural = "roles", singular = "role")
public class Role extends AbstractExtension {

    @Schema(minLength = 1)
    List<PolicyRule> rules;
}
