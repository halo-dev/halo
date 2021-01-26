package run.halo.app.handler.theme.config.support;

import java.util.Objects;
import java.util.Set;
import lombok.Data;

/**
 * Theme property.
 *
 * @author ryanwang
 * @author johnniang
 * @date 2019-03-22
 */
@Data
public class ThemeProperty {

    /**
     * Theme id.
     */
    private String id;

    /**
     * Theme name.
     */
    private String name;

    /**
     * Theme website.
     */
    private String website;

    /**
     * Theme remote branch.(default is master)
     */
    private String branch = "master";

    /**
     * Theme git repo url.
     */
    private String repo;

    /**
     * Theme update strategy. Default is branch.
     */
    private UpdateStrategy updateStrategy = UpdateStrategy.RELEASE;

    /**
     * Theme description.
     */
    private String description;

    /**
     * Theme logo.
     */
    private String logo;

    /**
     * Theme version.
     */
    private String version;

    /**
     * Require halo version.
     */
    private String require;

    /**
     * Theme author.
     */
    private Author author;

    /**
     * Theme full path.
     */
    private String themePath;

    /**
     * Theme folder name.
     */
    private String folderName;

    /**
     * Has options.
     */
    private boolean hasOptions;

    /**
     * Is activated.
     */
    private boolean isActivated;

    /**
     * Screenshots url.
     */
    private String screenshots;

    /**
     * Post preset metas.
     */
    private Set<String> postMetaField;

    /**
     * Sheet preset metas.
     */
    private Set<String> sheetMetaField;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ThemeProperty that = (ThemeProperty) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Theme author info.
     *
     * @author johnniang
     */
    @Data
    public static class Author {

        /**
         * Author name.
         */
        private String name;

        /**
         * Author website.
         */
        private String website;

        /**
         * Author avatar.
         */
        private String avatar;
    }

    /**
     * Theme update strategy.
     *
     * @author johnniang
     */
    public enum UpdateStrategy {

        /**
         * Update from specific branch
         */
        BRANCH,

        /**
         * Update from latest release, only available if the repo is a github repo
         */
        RELEASE;
    }
}
