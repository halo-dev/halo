package cc.ryanc.halo.web.controller.portal.api;

import cc.ryanc.halo.model.dto.TagOutputDTO;
import cc.ryanc.halo.service.PostTagService;
import cc.ryanc.halo.service.TagService;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Portal tag controller.
 *
 * @author johnniang
 * @date 4/2/19
 */
@RestController("PortalTagController")
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    private final PostTagService postTagService;

    public TagController(TagService tagService, PostTagService postTagService) {
        this.tagService = tagService;
        this.postTagService = postTagService;
    }

    @GetMapping
    @ApiOperation("Lists tags")
    public List<? extends TagOutputDTO> listTags(@SortDefault(sort = "updateTime", direction = Sort.Direction.DESC) Sort sort,
                                                 @RequestParam(name = "more", required = false, defaultValue = "false") Boolean more) {
        if (more) {
            return postTagService.listTagWithCountDtos(sort);
        }
        return tagService.convertTo(tagService.listAll(sort));
    }

}
