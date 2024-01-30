package run.halo.app.extension.index;

import org.springframework.lang.NonNull;
import run.halo.app.extension.Extension;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.Scheme;

/**
 * <p>{@link IndexSpecRegistry} is a registry for {@link IndexSpecs} to manage {@link IndexSpecs}
 * for different {@link Extension}.</p>
 *
 * @author guqing
 * @since 2.12.0
 */
public interface IndexSpecRegistry {
    /**
     * <p>Create a new {@link IndexSpecs} for the given {@link Scheme}.</p>
     * <p>The returned {@link IndexSpecs} is always includes some default {@link IndexSpec} that
     * does not need to be registered again:</p>
     * <ul>
     *     <li>{@link Metadata#getName()} for unique primary index spec named metadata_name</li>
     *     <li>{@link Metadata#getCreationTimestamp()} for creation_timestamp index spec</li>
     *     <li>{@link Metadata#getDeletionTimestamp()} for deletion_timestamp index spec</li>
     *     <li>{@link Metadata#getLabels()} for labels index spec</li>
     * </ul>
     *
     * @param scheme must not be {@literal null}.
     * @return the {@link IndexSpecs} for the given {@link Scheme}.
     */
    IndexSpecs indexFor(Scheme scheme);

    /**
     * Get {@link IndexSpecs} for the given {@link Scheme} type registered before.
     *
     * @param scheme must not be {@literal null}.
     * @return the {@link IndexSpecs} for the given {@link Scheme}.
     * @throws IllegalArgumentException if no {@link IndexSpecs} found for the given
     *                                  {@link Scheme}.
     */
    IndexSpecs getIndexSpecs(Scheme scheme);

    boolean contains(Scheme scheme);

    void removeIndexSpecs(Scheme scheme);

    /**
     * Get key space for an extension type.
     *
     * @param scheme is a scheme of an Extension.
     * @return key space(never null)
     */
    @NonNull
    String getKeySpace(Scheme scheme);
}
