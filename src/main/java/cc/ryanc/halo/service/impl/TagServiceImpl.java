package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.entity.Tag;
import cc.ryanc.halo.repository.TagRepository;
import cc.ryanc.halo.service.TagService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * TagService implementation class
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Service
public class TagServiceImpl extends AbstractCrudService<Tag, Integer> implements TagService {

    private TagRepository tagRepository;

    public TagServiceImpl(TagRepository tagRepository) {
        super(tagRepository);
        this.tagRepository = tagRepository;
    }
}
