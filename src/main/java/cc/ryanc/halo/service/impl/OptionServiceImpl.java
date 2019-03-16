package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.entity.Option;
import cc.ryanc.halo.repository.OptionRepository;
import cc.ryanc.halo.service.OptionService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import cc.ryanc.halo.utils.ServiceUtils;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * OptionService implementation class
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Service
public class OptionServiceImpl extends AbstractCrudService<Option, Integer> implements OptionService {

    private final OptionRepository optionRepository;

    public OptionServiceImpl(OptionRepository optionRepository) {
        super(optionRepository);
        this.optionRepository = optionRepository;
    }

    /**
     * Save one option
     *
     * @param key   key
     * @param value value
     */
    @Override
    public void saveOption(String key, String value) {
        if (StrUtil.equals(value, "")) {
            optionRepository.removeByOptionKey(key);
        } else if (StrUtil.isNotEmpty(key)) {
            Option options = optionRepository.findByOptionKey(key).map(option -> {
                // Exist
                option.setOptionValue(value);
                return option;
            }).orElseGet(() -> {
                // Not exist
                Option option = new Option();
                option.setOptionKey(key);
                option.setOptionValue(value);
                return option;
            });

            // Save or update the options
            optionRepository.save(options);
        }
    }

    /**
     * Save multiple options
     *
     * @param options options
     */
    @Override
    public void saveOptions(Map<String, String> options) {
        if (!CollectionUtils.isEmpty(options)) {
            options.forEach(this::saveOption);
        }
    }

    /**
     * Get all options
     *
     * @return Map
     */
    @Override
    public Map<String, String> listOptions() {
        return ServiceUtils.convertToMap(listAll(), Option::getOptionKey, Option::getOptionValue);
    }

    /**
     * Get option by key
     *
     * @param key key
     * @return String
     */
    @Override
    public String getByKey(String key) {
        return optionRepository.findByOptionKey(key).map(Option::getOptionValue).orElse(null);
    }
}
