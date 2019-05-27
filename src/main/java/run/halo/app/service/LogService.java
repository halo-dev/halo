package run.halo.app.service;

import org.springframework.data.domain.Page;
import run.halo.app.model.dto.LogDTO;
import run.halo.app.model.entity.Log;
import run.halo.app.service.base.CrudService;

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
    Page<LogDTO> pageLatest(int top);
}
