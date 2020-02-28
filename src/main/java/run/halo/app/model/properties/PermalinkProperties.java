package run.halo.app.model.properties;

import run.halo.app.model.enums.PostPermalinkType;

/**
 * Permalink properties enum.
 *
 * @author ryanwang
 * @date 2020-01-07
 */
public enum PermalinkProperties implements PropertyEnum {

    /**
     * Post Permalink type.
     */
    POST_PERMALINK_TYPE("post_permalink_type", PostPermalinkType.class, PostPermalinkType.DEFAULT.name()),

    /**
     * Categories prefix
     * such as: /categories or /categories/${slug}
     */
    CATEGORIES_PREFIX("categories_prefix", String.class, "categories"),

    /**
     * Tags prefix
     * such as: /tags or /tags/${slug}
     */
    TAGS_PREFIX("tags_prefix", String.class, "tags"),

    /**
     * Archives prefix.
     * such as: /archives
     */
    ARCHIVES_PREFIX("archives_prefix", String.class, "archives"),

    /**
     * Sheet prefix
     * such as: /s/${slug}
     */
    SHEET_PREFIX("sheet_prefix", String.class, "s"),

    /**
     * Links page prefix
     * default is links
     */
    LINKS_PREFIX("links_prefix", String.class, "links"),

    /**
     * Photos page prefix
     * default is photos
     */
    PHOTOS_PREFIX("photos_prefix", String.class, "photos"),

    /**
     * Journals page prefix
     * default is journals
     */
    JOURNALS_PREFIX("journals_prefix", String.class, "journals"),

    /**
     * Path suffix
     * such as: .html or .jsp
     */
    PATH_SUFFIX("path_suffix", String.class, "");

    private final String value;

    private final Class<?> type;

    private final String defaultValue;

    PermalinkProperties(String value, Class<?> type, String defaultValue) {
        this.value = value;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public String defaultValue() {
        return defaultValue;
    }

    @Override
    public String getValue() {
        return value;
    }
}
