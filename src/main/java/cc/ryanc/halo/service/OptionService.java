package cc.ryanc.halo.service;

import cc.ryanc.halo.model.entity.Option;
import cc.ryanc.halo.service.base.CrudService;

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
     * @param key   key
     * @param value value
     */
    void saveOption(String key, String value);

    /**
     * Save multiple options
     *
     * @param options options
     */
    void saveOptions(Map<String, String> options);

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
