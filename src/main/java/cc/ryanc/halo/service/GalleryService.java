package cc.ryanc.halo.service;

import cc.ryanc.halo.model.dto.GalleryOutputDTO;
import cc.ryanc.halo.model.entity.Gallery;
import cc.ryanc.halo.service.base.CrudService;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * Gallery service.
 *
 * @author johnniang
 */
public interface GalleryService extends CrudService<Gallery, Integer> {

    /**
     * List gallery dtos.
     *
     * @param sort sort
     * @return all galleries
     */
    List<GalleryOutputDTO> listDtos(@NonNull Sort sort);
}
