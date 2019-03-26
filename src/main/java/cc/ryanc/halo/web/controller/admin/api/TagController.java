package cc.ryanc.halo.web.controller.admin.api;

import cc.ryanc.halo.model.dto.TagOutputDTO;
import cc.ryanc.halo.model.entity.Tag;
import cc.ryanc.halo.model.params.TagParam;
import cc.ryanc.halo.service.PostTagService;
import cc.ryanc.halo.service.TagService;
import io.swagger.annotations.ApiOperation;
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

    @GetMapping
    public List<? extends TagOutputDTO> listTags(@SortDefault(sort = "updateTime", direction = Sort.Direction.DESC) Sort sort,
                                                 @RequestParam(name = "more", required = false, defaultValue = "false") Boolean more) {
        if (more) {
            return postTagService.listTagWithCountDtos(sort);
        }
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

    /**
     * Get tag by id
     *
     * @param tagId tag id
     * @return TagOutputDTO
     */
    @GetMapping("{tagId:\\d+}")
    @ApiOperation("Get tag detail by id")
    public TagOutputDTO getBy(@PathVariable("tagId") Integer tagId) {
        return new TagOutputDTO().convertFrom(tagService.getById(tagId));
    }

    @PutMapping("{tagId:\\d+}")
    @ApiOperation("Updates tag")
    public TagOutputDTO updateBy(@PathVariable("tagId") Integer tagId,
                                 @Valid @RequestBody TagParam tagParam) {
        // Get old tag
        Tag tag = tagService.getById(tagId);

        // Update tag
        tagParam.update(tag);

        // Update tag
        return new TagOutputDTO().convertFrom(tagService.update(tag));
    }

    /**
     * Delete tag by id.
     *
     * @param tagId tag id
     */
    @DeleteMapping("{tagId:\\d+}")
    @ApiOperation("Delete tag by id")
    public void deletePermanently(@PathVariable("tagId") Integer tagId) {
        // Remove the tag
        tagService.removeById(tagId);
        // Remove the post tag relationship
        postTagService.removeByTagId(tagId);
    }
}
