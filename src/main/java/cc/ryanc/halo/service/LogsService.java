package cc.ryanc.halo.service;

import cc.ryanc.halo.model.domain.Logs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
     * @param logTitle   logTitle
     * @param logContent logContent
     * @param request    request
     */
    void save(String logTitle, String logContent, HttpServletRequest request);

    /**
     * 移除所有日志
     */
    void removeAll();

    /**
     * 查询所有日志并分页
     *
     * @param pageable pageable
     * @return Page
     */
    Page<Logs> findAll(Pageable pageable);

    /**
     * 查询最新的五条日志
     *
     * @return List
     */
    List<Logs> findLogsLatest();
}
