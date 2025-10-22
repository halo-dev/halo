package run.halo.app.extension.index;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.Nullable;

/**
 * String key wrapper for nullable string comparison. Only for backward compatibility. May remove
 * in the future.
 *
 * @author johnniang
 * @since 2.22.0
 */
@Deprecated(forRemoval = true, since = "2.22.0")
record UnknownKey(@Nullable String value) implements Comparable<UnknownKey> {

    @Override
    public int compareTo(@NotNull UnknownKey o) {
        return KeyComparator.INSTANCE.compare(this.value, o.value);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UnknownKey unknownKey = (UnknownKey) o;
        return Objects.equals(value, unknownKey.value);
    }

    @NotNull
    @Override
    public String toString() {
        return Objects.toString(value);
    }
}
