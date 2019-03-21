package cc.ryanc.halo.service;

import cc.ryanc.halo.model.dto.TagOutputDTO;
import cc.ryanc.halo.model.entity.Tag;
import cc.ryanc.halo.service.base.CrudService;

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

}
