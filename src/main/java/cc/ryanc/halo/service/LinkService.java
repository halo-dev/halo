package cc.ryanc.halo.service;

import cc.ryanc.halo.model.dto.LinkOutputDTO;
import cc.ryanc.halo.model.entity.Link;
import cc.ryanc.halo.model.params.LinkParam;
import cc.ryanc.halo.model.vo.LinkTeamVO;
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
    @NonNull
    List<LinkOutputDTO> listDtos(@NonNull Sort sort);

    /**
     * List link by group
     *
     * @return a list of link team vo
     */
    @NonNull
    List<LinkTeamVO> listTeamVos();

    /**
     * Lists link team vos.
     *
     * @param sort must not be null
     * @return a list of link team vo
     */
    @NonNull
    List<LinkTeamVO> listTeamVos(@NonNull Sort sort);

    /**
     * Creates link by link param.
     *
     * @param linkParam must not be null
     * @return create link
     */
    @NonNull
    Link createBy(@NonNull LinkParam linkParam);

    /**
     * Exists by link name.
     *
     * @param name must not be blank
     * @return true if exists; false otherwise
     */
    boolean existByName(String name);
}
