package cc.ryanc.halo.service;

import cc.ryanc.halo.model.domain.Link;
import cc.ryanc.halo.model.domain.Logs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * description :
 * @date : 2018/1/19
 */
public interface LogsService {

    /**
     * 保存日志
     * @param logs logs
     * @return logs
     */
    Logs saveByLogs(Logs logs);

    /**
     * 根据编号移除
     * @param logsId logsId
     * @return Logs
     */
    void removeByLogsId(Integer logsId);

    /**
     * 移除所有日志
     */
    void removeAllLogs();

    /**
     * 查询所有日志并分页
     * @param pageable pageable
     * @return page
     */
    Page<Logs> findAllLogs(Pageable pageable);

    /**
     * 查询最新的五条日志
     * @return list
     */
    List<Logs> findLogsLatest();

    /**
     * 根据编号查询
     * @param logsId logsId
     * @return logs
     */
    Logs findLogsByLogsId(Integer logsId);
}
