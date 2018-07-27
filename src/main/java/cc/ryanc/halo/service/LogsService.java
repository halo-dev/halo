package cc.ryanc.halo.service;

import cc.ryanc.halo.model.domain.Logs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * <pre>
 *     日志业务逻辑接口
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/1/19
 */
public interface LogsService {

    /**
     * 保存日志
     *
     * @param logs logs
     * @return Logs
     */
    Logs saveByLogs(Logs logs);

    /**
     * 根据编号移除
     *
     * @param logsId logsId
     */
    void removeByLogsId(Long logsId);

    /**
     * 移除所有日志
     */
    void removeAllLogs();

    /**
     * 查询所有日志并分页
     *
     * @param pageable pageable
     * @return Page
     */
    Page<Logs> findAllLogs(Pageable pageable);

    /**
     * 查询最新的五条日志
     *
     * @return List
     */
    List<Logs> findLogsLatest();

    /**
     * 根据编号查询
     *
     * @param logsId logsId
     * @return Optional
     */
    Optional<Logs> findLogsByLogsId(Long logsId);
}
