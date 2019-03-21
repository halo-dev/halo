package cc.ryanc.halo.service;

import cc.ryanc.halo.model.dto.LinkOutputDTO;
import cc.ryanc.halo.model.entity.Link;
import cc.ryanc.halo.service.base.CrudService;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * Link service.
 *
 * @author johnniang
 */
public interface LinkService extends CrudService<Link, Integer> {

    /**
     * List link dtos.
     *
     * @param sort sort
     * @return all links
     */
    List<LinkOutputDTO> listDtos(@NonNull Sort sort);
}
