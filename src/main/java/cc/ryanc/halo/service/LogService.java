package cc.ryanc.halo.service;

import cc.ryanc.halo.model.dto.LogOutputDTO;
import cc.ryanc.halo.model.entity.Log;
import cc.ryanc.halo.service.base.CrudService;
import org.springframework.data.domain.Page;

/**
 * Log service.
 *
 * @author johnniang
 */
public interface LogService extends CrudService<Log, Long> {

    /**
     * Lists latest logs.
     *
     * @param top top number must not be less than 0
     * @return a page of latest logs
     */
    Page<LogOutputDTO> pageLatest(int top);
}
