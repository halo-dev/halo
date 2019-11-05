package run.halo.app.repository;

import run.halo.app.model.entity.Option;
import run.halo.app.repository.base.BaseRepository;

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
    Optional<Option> findByKey(String key);

    /**
     * Delete option by key
     *
     * @param key key
     */
    void deleteByKey(String key);
}
