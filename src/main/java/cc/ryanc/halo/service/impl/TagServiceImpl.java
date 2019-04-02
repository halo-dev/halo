package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.exception.AlreadyExistsException;
import cc.ryanc.halo.exception.NotFoundException;
import cc.ryanc.halo.model.dto.TagOutputDTO;
import cc.ryanc.halo.model.entity.Tag;
import cc.ryanc.halo.repository.TagRepository;
import cc.ryanc.halo.service.TagService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
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

    /**
     * Get tag by slug name
     *
     * @param slugName slug name
     * @return Tag
     */
    @Override
    public Tag getBySlugNameOfNonNull(String slugName) {
        return tagRepository.getBySlugName(slugName).orElseThrow(() -> new NotFoundException("The tag does not exist").setErrorData(slugName));
    }

    @Override
    public List<TagOutputDTO> convertTo(List<Tag> tags) {
        return CollectionUtils.isEmpty(tags) ?
                Collections.emptyList() :
                tags.stream()
                        .map(tag -> (TagOutputDTO) new TagOutputDTO().convertFrom(tag))
                        .collect(Collectors.toList());
    }
}
