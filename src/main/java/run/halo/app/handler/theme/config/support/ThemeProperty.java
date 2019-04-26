package run.halo.app.handler.theme.config.support;

import lombok.Data;

import java.util.Objects;

/**
 * Theme property.
 *
 * @author ryanwang
 * @date : 2019-03-22
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
     * Theme author.
     */
    private Author author;

    /**
     * Theme path.
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThemeProperty that = (ThemeProperty) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
