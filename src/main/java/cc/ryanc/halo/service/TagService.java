package cc.ryanc.halo.service;

import cc.ryanc.halo.model.dto.TagOutputDTO;
import cc.ryanc.halo.model.entity.Tag;
import cc.ryanc.halo.service.base.CrudService;
import org.springframework.lang.NonNull;

import java.util.List;


/**
 * Tag service.
 *
 * @author johnniang
 */
public interface TagService extends CrudService<Tag, Integer> {

    /**
     * Get tag by slug name
     *
     * @param slugName slug name
     * @return Tag
     */
    @NonNull
    Tag getBySlugNameOfNonNull(@NonNull String slugName);

    /**
     * Converts to tag output dtos.
     *
     * @param tags tag list
     * @return a list of tag output dto
     */
    @NonNull
    List<TagOutputDTO> convertTo(List<Tag> tags);
}
