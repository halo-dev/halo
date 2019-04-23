package run.halo.app.service;

import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import run.halo.app.model.dto.GalleryDTO;
import run.halo.app.model.entity.Gallery;
import run.halo.app.model.vo.GalleryTeamVO;
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
    List<GalleryDTO> listDtos(@NonNull Sort sort);

    /**
     * Lists gallery team vos.
     *
     * @param sort must not be null
     * @return a list of gallery team vo
     */
    List<GalleryTeamVO> listTeamVos(@NonNull Sort sort);

    /**
     * List galleries by team.
     *
     * @param team team
     * @param sort sort
     * @return list of galleries
     */
    List<GalleryDTO> listByTeam(@NonNull String team, Sort sort);
}
