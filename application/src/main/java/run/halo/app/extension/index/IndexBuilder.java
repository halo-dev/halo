package run.halo.app.extension.index;

import org.springframework.lang.NonNull;

/**
 * {@link IndexBuilder} is used to build index for a specific
 * {@link run.halo.app.extension.Extension} type on startup.
 *
 * @author guqing
 * @since 2.12.0
 */
public interface IndexBuilder {
    /**
     * Start building index for a specific {@link run.halo.app.extension.Extension} type.
     */
    void startBuildingIndex();

    /**
     * Gets final index entries after building index.
     *
     * @return index entries must not be null.
     * @throws IllegalStateException if any index entries are not ready yet.
     */
    @NonNull
    IndexEntryContainer getIndexEntries();
}
