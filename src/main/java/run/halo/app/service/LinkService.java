package run.halo.app.service;

import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import run.halo.app.model.dto.LinkDTO;
import run.halo.app.model.entity.Link;
import run.halo.app.model.params.LinkParam;
import run.halo.app.model.vo.LinkTeamVO;
import run.halo.app.service.base.CrudService;

import java.util.List;

/**
 * Link service interface.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-14
 */
public interface LinkService extends CrudService<Link, Integer> {

    /**
     * List link dtos.
     *
     * @param sort sort
     * @return all links
     */
    @NonNull
    List<LinkDTO> listDtos(@NonNull Sort sort);

    /**
     * Lists link team vos.
     *
     * @param sort must not be null
     * @return a list of link team vo
     */
    @NonNull
    List<LinkTeamVO> listTeamVos(@NonNull Sort sort);

    /**
     * Lists link team vos by random
     *
     * @param sort
     * @return a list of link team vo by random
     */
    @NonNull
    List<LinkTeamVO> listTeamVosByRandom(@NonNull Sort sort);

    /**
     * Creates link by link param.
     *
     * @param linkParam must not be null
     * @return create link
     */
    @NonNull
    Link createBy(@NonNull LinkParam linkParam);

    /**
     * Updates link by link param.
     *
     *
     * @param id must not be null
     * @param linkParam must not be null
     * @return updated link
     */
    @NonNull
    Link updateBy(Integer id, @NonNull LinkParam linkParam);

    /**
     * Exists by link name.
     *
     * @param name must not be blank
     * @return true if exists; false otherwise
     */
    boolean existByName(String name);

    /**
     * Exists by link url.
     *
     * @param url must not be blank
     * @return true if exists; false otherwise
     */
    boolean existByUrl(String url);

    /**
     * List all link teams.
     *
     * @return a list of teams.
     */
    List<String> listAllTeams();

    /**
     * List all link teams by random
     *
     * @return a list of teams by random
     */
    @NonNull
    List<Link> listAllByRandom();
}
