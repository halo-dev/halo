package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.exception.AlreadyExistsException;
import cc.ryanc.halo.model.dto.TagOutputDTO;
import cc.ryanc.halo.model.entity.Tag;
import cc.ryanc.halo.repository.TagRepository;
import cc.ryanc.halo.service.TagService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TagService implementation class
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Slf4j
@Service
public class TagServiceImpl extends AbstractCrudService<Tag, Integer> implements TagService {

    private final TagRepository tagRepository;

    public TagServiceImpl(TagRepository tagRepository) {
        super(tagRepository);
        this.tagRepository = tagRepository;
    }

    /**
     * Remove tag and relationship
     *
     * @param id id
     */
    @Override
    public void remove(Integer id) {
        // TODO 删除标签，以及对应的文章关系
    }

    @Override
    public List<TagOutputDTO> listDtos(Sort sort) {
        Assert.notNull(sort, "Sort info must not be null");

        return listAll(sort).stream().map(tag -> (TagOutputDTO) new TagOutputDTO().convertFrom(tag)).collect(Collectors.toList());
    }

    @Override
    public Tag create(Tag tag) {
        // Check if the tag is exist
        long count = tagRepository.countByNameOrSlugName(tag.getName(), tag.getSlugName());

        log.debug("Tag count: [{}]", count);

        if (count > 0) {
            // If the tag has exist already
            throw new AlreadyExistsException("The tag has already exist").setErrorData(tag);
        }

        // Get tag name
        return super.create(tag);
    }
}
