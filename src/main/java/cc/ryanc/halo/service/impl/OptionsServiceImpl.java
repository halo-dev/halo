package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.domain.Options;
import cc.ryanc.halo.repository.OptionsRepository;
import cc.ryanc.halo.service.OptionsService;
import cc.ryanc.halo.util.HaloUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2017/11/14
 * description:
 */
@Service
public class OptionsServiceImpl implements OptionsService {

    @Autowired
    private OptionsRepository optionsRepository;

    private static final String OPTIONS_KEY = "'options_key'";

    private static final String OPTIONS_CACHE_NAME = "options_cache";

    /**
     * 批量保存设置
     * @param options options
     */
    @CacheEvict(value = OPTIONS_CACHE_NAME,key = OPTIONS_KEY)
    @Override
    public void saveOptions(Map<String,String> options){
        if(null != options && !options.isEmpty()){
            options.forEach((k,v) -> saveOption(k,v));
        }
    }

    /**
     * 保存单个设置选项
     * @param key key
     * @param value value
     */
    @CacheEvict(value = OPTIONS_CACHE_NAME,key = OPTIONS_KEY)
    @Override
    public void saveOption(String key,String value){
        Options options = null;
        if("".equals(value)){
            options = new Options();
            options.setOptionName(key);
            this.removeOption(options);
        }else {
            if (HaloUtil.isNotNull(key)) {
                //如果查询到有该设置选项则做更新操作，反之保存新的设置选项
                if (null == optionsRepository.findOptionsByOptionName(key)) {
                    options = new Options();
                    options.setOptionName(key);
                    options.setOptionValue(value);
                    optionsRepository.save(options);
                } else {
                    options = optionsRepository.findOptionsByOptionName(key);
                    options.setOptionValue(value);
                    optionsRepository.save(options);
                }
            }
        }
    }

    /**
     * 移除设置项
     * @param options options
     */
    @CacheEvict(value = OPTIONS_CACHE_NAME,key = OPTIONS_KEY)
    @Override
    public void removeOption(Options options) {
        optionsRepository.delete(options);
    }

    /**
     * 获取设置选项
     * @return map
     */
    @Cacheable(value = OPTIONS_CACHE_NAME,key = OPTIONS_KEY)
    @Override
    public Map<String, String> findAllOptions() {
        Map<String,String> options = new HashMap<String,String>();
        List<Options> optionsList = optionsRepository.findAll();
        if(null != optionsList){
            optionsList.forEach(option -> options.put(option.getOptionName(),option.getOptionValue()));
        }
        return options;
    }

    /**
     * 根据key查询单个设置选项
     * @param key key
     * @return String
     */
    @Cacheable(value = OPTIONS_CACHE_NAME,key = "#key+'options'")
    @Override
    public String findOneOption(String key) {
        Options options = optionsRepository.findOptionsByOptionName(key);
        if(null!=options){
            return options.getOptionValue();
        }
        return null;
    }
}
