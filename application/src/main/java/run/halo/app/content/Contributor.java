package run.halo.app.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * User summary used when displaying content authorship.
 *
 * @author guqing
 * @since 2.0.0
 */
@Schema(description = "User summary for a content owner or contributor.")
@Data
public class Contributor {
    /** User display name. */
    private String displayName;

    /** User avatar URL. */
    private String avatar;

    /** User metadata name. */
    private String name;
}
