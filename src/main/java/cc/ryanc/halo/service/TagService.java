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
     * Get tag by slug name
     *
     * @param slugName slug name
     * @return Tag
     */
    Tag getBySlugName(@NonNull String slugName);

    /**
     * Converts to tag output dtos.
     *
     * @param tags tag list
     * @return a list of tag output dto
     */
    @NonNull
    List<TagOutputDTO> convertTo(List<Tag> tags);
}
