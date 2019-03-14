package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.entity.Log;
import cc.ryanc.halo.repository.LogRepository;
import cc.ryanc.halo.service.LogService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * LogService implementation class
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Service
public class LogServiceImpl extends AbstractCrudService<Log, Long> implements LogService {

    private LogRepository logRepository;

    public LogServiceImpl(LogRepository logRepository) {
        super(logRepository);
        this.logRepository = logRepository;
    }
}
