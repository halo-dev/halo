package cc.ryanc.halo.web.controller.admin.api;

import cc.ryanc.halo.model.dto.TagOutputDTO;
import cc.ryanc.halo.model.dto.TagWithCountOutputDTO;
import cc.ryanc.halo.model.entity.Tag;
import cc.ryanc.halo.model.params.TagParam;
import cc.ryanc.halo.service.PostTagService;
import cc.ryanc.halo.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Tag controller.
 *
 * @author johnniang
 * @date 3/20/19
 */
@Slf4j
@RestController
@RequestMapping("/admin/api/tags")
public class TagController {

    private final TagService tagService;

    private final PostTagService postTagService;

    public TagController(TagService tagService,
                         PostTagService postTagService) {
        this.tagService = tagService;
        this.postTagService = postTagService;
    }

    @GetMapping("/addition")
    public List<TagWithCountOutputDTO> listTagsWithCount(@SortDefault(sort = "updateTime", direction = Sort.Direction.DESC) Sort sort) {
        return postTagService.listTagWithCountDtos(sort);
    }

    @GetMapping
    public List<TagOutputDTO> listTags(@SortDefault(sort = "updateTime", direction = Sort.Direction.DESC) Sort sort) {
        return tagService.convertTo(tagService.listAll(sort));
    }

    @PostMapping
    public TagOutputDTO createTag(@Valid @RequestBody TagParam tagParam) {
        // Convert to tag
        Tag tag = tagParam.convertTo();

        log.debug("Tag to be created: [{}]", tag);

        // Create and convert
        return new TagOutputDTO().convertFrom(tagService.create(tag));
    }
}
