package cc.ryanc.halo.web.controller.admin.api;

import cc.ryanc.halo.model.dto.TagOutputDTO;
import cc.ryanc.halo.model.entity.Tag;
import cc.ryanc.halo.model.params.TagParam;
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

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public List<TagOutputDTO> listTags(@SortDefault(sort = "updateTime", direction = Sort.Direction.DESC) Sort sort) {

        return null;
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
