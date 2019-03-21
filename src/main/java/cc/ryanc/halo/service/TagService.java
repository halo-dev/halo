package cc.ryanc.halo.service;

import cc.ryanc.halo.model.dto.TagOutputDTO;
import cc.ryanc.halo.model.entity.Tag;
import cc.ryanc.halo.service.base.CrudService;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

import java.util.List;


/**
 * Tag service.
 *
 * @author johnniang
 */
public interface TagService extends CrudService<Tag, Integer> {

    /**
     * Remove tag and relationship
     *
     * @param id id
     */
    void remove(Integer id);

    /**
     * Lists all tag dtos.
     *
     * @param sort sort info must not be null
     * @return a list of tag dto
     */
    @NonNull
    List<TagOutputDTO> listDtos(@NonNull Sort sort);

    /**
     * Get tag by slug name
     *
     * @param slugName slug name
     * @return Tag
     */
    Tag getBySlugName(@NonNull String slugName);
}
