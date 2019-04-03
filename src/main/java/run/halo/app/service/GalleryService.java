package run.halo.app.service;

import run.halo.app.model.dto.GalleryOutputDTO;
import run.halo.app.model.entity.Gallery;
import run.halo.app.service.base.CrudService;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import run.halo.app.service.base.CrudService;

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
