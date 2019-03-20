package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.entity.Tag;
import cc.ryanc.halo.repository.base.BaseRepository;
import org.springframework.lang.NonNull;

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
}
