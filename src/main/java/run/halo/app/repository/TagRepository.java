package run.halo.app.repository;

import org.springframework.lang.NonNull;
import run.halo.app.model.entity.Tag;
import run.halo.app.repository.base.BaseRepository;

import java.util.Optional;

/**
 * Tag repository.
 *
 * @author johnniang
 */
public interface TagRepository extends BaseRepository<Tag, Integer> {

    /**
     * Count by name or slug name.
     *
     * @param name     tag name must not be null
     * @param slugName tag slug name must not be null
     * @return tag count
     */
    long countByNameOrSlugName(@NonNull String name, @NonNull String slugName);

    /**
     * Get tag by slug name
     *
     * @param slugName slug name must not be null.
     * @return an optional of slug name.
     */
    Optional<Tag> getBySlugName(@NonNull String slugName);

    /**
     * Get tag by name
     *
     * @param name name must not be null.
     * @return an optional of tag
     */
    Optional<Tag> getByName(@NonNull String name);
}
