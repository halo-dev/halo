package run.halo.app.content;

import lombok.Data;

/**
 * Contributor from user.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
public class Contributor {
    private String displayName;
    private String avatar;
    private String name;

    public static Contributor getGhost() {
        Contributor contributor = new Contributor();
        contributor.setName("ghost");
        contributor.setDisplayName("已删除用户");
        return contributor;
    }
}
