package run.halo.app.model.support;

import lombok.Data;

/**
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
    private String author;

    /**
     * Theme author website.
     */
    private String authorWebsite;

    /**
     * Folder name.
     */
    private String folderName;

    /**
     * Has options.
     */
    private Boolean hasOptions;
}
