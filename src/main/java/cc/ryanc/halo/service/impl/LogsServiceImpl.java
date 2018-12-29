package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.domain.Logs;
import cc.ryanc.halo.repository.LogsRepository;
import cc.ryanc.halo.service.LogsService;
import cn.hutool.extra.servlet.ServletUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * <pre>
 *     日志业务逻辑实现类
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/1/19
 */
@Service
public class LogsServiceImpl implements LogsService {

    @Autowired
    private LogsRepository logsRepository;

    /**
     * 保存日志
     *
     * @param logTitle   logTitle
     * @param logContent logContent
     * @param request    request
     */
    @Override
    public void save(String logTitle, String logContent, HttpServletRequest request) {
        final Logs logs = new Logs();
        logs.setLogTitle(logTitle);
        logs.setLogContent(logContent);
        logs.setLogCreated(new Date());
        logs.setLogIp(ServletUtil.getClientIP(request));
        logsRepository.save(logs);
    }

    /**
     * 移除所有日志
     */
    @Override
    public void removeAll() {
        logsRepository.deleteAll();
    }

    /**
     * 查询所有日志并分页
     *
     * @param pageable pageable
     * @return Page
     */
    @Override
    public Page<Logs> findAll(Pageable pageable) {
        return logsRepository.findAll(pageable);
    }

    /**
     * 查询最新的五条日志
     *
     * @return List
     */
    @Override
    public List<Logs> findLogsLatest() {
        return logsRepository.findTopFive();
    }
}
