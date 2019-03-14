package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.dto.LogOutputDTO;
import cc.ryanc.halo.model.entity.Log;
import cc.ryanc.halo.repository.LogRepository;
import cc.ryanc.halo.service.LogService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * LogService implementation class
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Service
public class LogServiceImpl extends AbstractCrudService<Log, Long> implements LogService {

    private final LogRepository logRepository;

    public LogServiceImpl(LogRepository logRepository) {
        super(logRepository);
        this.logRepository = logRepository;
    }

    @Override
    public Page<LogOutputDTO> listLatest(int top) {
        Assert.isTrue(top > 0, "Top number must not be less than 0");

        // Build page request
        PageRequest latestPageable = PageRequest.of(0, top, Sort.by(Sort.Direction.DESC, "createTime"));

        // List all
        return listAll(latestPageable).map(log -> new LogOutputDTO().convertFrom(log));
    }
}
