package cc.ryanc.halo.service;

import cc.ryanc.halo.model.entity.Option;
import cc.ryanc.halo.model.enums.BlogProperties;
import cc.ryanc.halo.service.base.CrudService;
import org.springframework.lang.NonNull;

import java.util.Map;

/**
 * Option service.
 *
 * @author johnniang
 */
public interface OptionService extends CrudService<Option, Integer> {

    /**
     * Save one option
     *
     * @param key   key must not be blank
     * @param value value
     */
    void save(@NonNull String key, String value);

    /**
     * Save multiple options
     *
     * @param options options
     */
    void save(@NonNull Map<String, String> options);

    /**
     * Saves blog properties.
     *
     * @param properties blog properties
     */
    void saveProperties(@NonNull Map<BlogProperties, String> properties);

    /**
     * Get all options
     *
     * @return Map
     */
    Map<String, String> listOptions();

    /**
     * Get option by key
     *
     * @param key key
     * @return String
     */
    String getByKey(String key);
}
