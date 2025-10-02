package run.halo.app.core.attachment;

import java.util.Arrays;
import java.util.Optional;
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

    public static Optional<ThumbnailSize> optionalValueOf(String name) {
        for (ThumbnailSize value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    public static Integer[] allowedWidths() {
        return Arrays.stream(ThumbnailSize.values())
            .map(ThumbnailSize::getWidth)
            .toArray(Integer[]::new);
    }

}
