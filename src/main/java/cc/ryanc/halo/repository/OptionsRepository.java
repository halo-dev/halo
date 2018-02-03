package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.domain.Options;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author : RYAN0UP
 * @date : 2017/11/14
 * @version : 1.0
 * description: 设置选项持久层
 */
public interface OptionsRepository extends JpaRepository<Options,Long>{

    /**
     * 根据key查询单个option
     *
     * @param key key
     * @return String
     */
    Options findOptionsByOptionName(String key);
}
