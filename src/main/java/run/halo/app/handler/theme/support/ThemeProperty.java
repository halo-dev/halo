package run.halo.app.handler.theme.support;

import lombok.Data;

/**
 * Theme property.
 *
 * @author : RYAN0UP
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
     * Folder name.
     */
    private String themePath;

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
}
