package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.domain.Logs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * <pre>
 *     日志持久层
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/1/19
 */
public interface LogsRepository extends JpaRepository<Logs, Long> {

    /**
     * 查询最新的五条数据
     *
     * @return List
     */
    @Query(value = "SELECT * FROM halo_logs ORDER BY log_created DESC LIMIT 5", nativeQuery = true)
    List<Logs> findTopFive();
}
