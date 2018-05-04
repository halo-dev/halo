package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.domain.Logs;
import cc.ryanc.halo.repository.LogsRepository;
import cc.ryanc.halo.service.LogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2018/1/19
 */
@Service
public class LogsServiceImpl implements LogsService {

    @Autowired
    private LogsRepository logsRepository;

    /**
     * 保存日志
     *
     * @param logs logs
     * @return logs
     */
    @Override
    public Logs saveByLogs(Logs logs) {
        return logsRepository.save(logs);
    }

    /**
     * 根据编号移除
     *
     * @param logsId logsId
     */
    @Override
    public void removeByLogsId(Long logsId) {
        Optional<Logs> logs = this.findLogsByLogsId(logsId);
        logsRepository.delete(logs.get());
    }

    /**
     * 移除所有日志
     */
    @Override
    public void removeAllLogs() {
        logsRepository.deleteAll();
    }

    /**
     * 查询所有日志并分页
     *
     * @param pageable pageable
     * @return page
     */
    @Override
    public Page<Logs> findAllLogs(Pageable pageable) {
        return logsRepository.findAll(pageable);
    }

    /**
     * 查询最新的五条日志
     *
     * @return list
     */
    @Override
    public List<Logs> findLogsLatest() {
        return logsRepository.findTopFive();
    }

    /**
     * 根据编号查询
     *
     * @param logsId logsId
     * @return logs
     */
    @Override
    public Optional<Logs> findLogsByLogsId(Long logsId) {
        return logsRepository.findById(logsId);
    }
}
