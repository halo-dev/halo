package run.halo.app.core.attachment;

import lombok.Getter;

@Getter
public enum ThumbnailSize {
    S(400),
    M(800),
    L(1200),
    XL(1600);

    private final int width;

    ThumbnailSize(int width) {
        this.width = width;
    }

    /**
     * Convert width string to {@link ThumbnailSize}.
     *
     * @param width width string
     */
    public static ThumbnailSize fromWidth(String width) {
        for (ThumbnailSize value : values()) {
            if (String.valueOf(value.getWidth()).equals(width)) {
                return value;
            }
        }
        return ThumbnailSize.M;
    }

    /**
     * Convert name to {@link ThumbnailSize}.
     */
    public static ThumbnailSize fromName(String name) {
        for (ThumbnailSize value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        throw new IllegalArgumentException("No such thumbnail size: " + name);
    }
}
