package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.entity.Option;
import cc.ryanc.halo.repository.base.BaseRepository;

import java.util.Optional;

/**
 * Option repository.
 *
 * @author johnniang
 */
public interface OptionRepository extends BaseRepository<Option, Integer> {

    /**
     * Query option by key
     *
     * @param key key
     * @return Option
     */
    Optional<Option> findByOptionKey(String key);

    /**
     * Delete option by key
     *
     * @param key key
     */
    void removeByOptionKey(String key);
}
