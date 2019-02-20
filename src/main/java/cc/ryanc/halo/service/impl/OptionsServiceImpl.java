package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.domain.Options;
import cc.ryanc.halo.repository.OptionsRepository;
import cc.ryanc.halo.service.OptionsService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import cc.ryanc.halo.utils.ServiceUtils;
import cn.hutool.core.util.StrUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *     系统设置业务逻辑实现类
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/11/14
 */
@Service
public class OptionsServiceImpl extends AbstractCrudService<Options, String> implements OptionsService {

    private static final String POSTS_CACHE_NAME = "posts";

    private final OptionsRepository optionsRepository;

    public OptionsServiceImpl(OptionsRepository optionsRepository) {
        super(optionsRepository);
        this.optionsRepository = optionsRepository;
    }

    /**
     * 批量保存设置
     *
     * @param options options
     */
    @Override
    @CacheEvict(value = POSTS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public void saveOptions(Map<String, String> options) {
        if (!CollectionUtils.isEmpty(options)) {
            options.forEach(this::saveOption);
        }
    }

    /**
     * 保存单个设置选项
     *
     * @param key   key
     * @param value value
     */
    @Override
    public void saveOption(String key, String value) {
        if (StrUtil.equals(value, "")) {
//            options = new Options();
//            options.setOptionName(key);
//            this.remove(options);

            removeByIdOfNullable(key);
        } else if (StrUtil.isNotEmpty(key)) {
            //如果查询到有该设置选项则做更新操作，反之保存新的设置选项

//                if (null == optionsRepository.findOptionsByOptionName(key)) {
//                    options = new Options();
//                    options.setOptionName(key);
//                    options.setOptionValue(value);
//                    optionsRepository.save(options);
//                } else {
//                    options = optionsRepository.findOptionsByOptionName(key);
//                    options.setOptionValue(value);
//                    optionsRepository.save(options);
//                }

            Options options = fetchById(key).map(option -> {
                // Exist
                option.setOptionValue(value);
                return option;
            }).orElseGet(() -> {
                // Not exist
                Options option = new Options();
                option.setOptionName(key);
                option.setOptionValue(value);
                return option;
            });

            // Save or update the options
            optionsRepository.save(options);
        }
    }

    /**
     * 获取设置选项
     *
     * @return Map
     */
    @Override
    public Map<String, String> findAllOptions() {
//        final Map<String, String> options = new HashMap<>();
//        final List<Options> optionsList = optionsRepository.findAll();
//        if (null != optionsList) {
//            optionsList.forEach(option -> options.put(option.getOptionName(), option.getOptionValue()));
//        }
//        return options;

        return ServiceUtils.convertToMap(listAll(), Options::getOptionName, Options::getOptionValue);
    }

    /**
     * 根据key查询单个设置选项
     *
     * @param key key
     * @return String
     */
    @Override
    public String findOneOption(String key) {
//        final Options options = getByIdOfNullable(key);
//        if (null != options) {
//            return options.getOptionValue();
//        }
//        return null;

        return fetchById(key).map(Options::getOptionValue).orElse(null);
    }
}
