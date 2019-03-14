package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.entity.Option;
import cc.ryanc.halo.repository.OptionRepository;
import cc.ryanc.halo.service.OptionService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import org.springframework.stereotype.Service;

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
}
