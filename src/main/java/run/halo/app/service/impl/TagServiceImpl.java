package run.halo.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.exception.AlreadyExistsException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.dto.TagDTO;
import run.halo.app.model.entity.Tag;
import run.halo.app.repository.TagRepository;
import run.halo.app.service.TagService;
import run.halo.app.service.base.AbstractCrudService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TagService implementation class
 *
 * @author ryanwang
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
    public TagDTO convertTo(Tag tag) {
        Assert.notNull(tag, "Tag must not be null");

        return new TagDTO().convertFrom(tag);
    }

    @Override
    public List<TagDTO> convertTo(List<Tag> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return Collections.emptyList();
        }

        return tags.stream()
                .map(this::convertTo)
                .collect(Collectors.toList());
    }
}
