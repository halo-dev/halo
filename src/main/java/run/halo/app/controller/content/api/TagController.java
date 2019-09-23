package run.halo.app.controller.content.api;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.dto.TagDTO;
import run.halo.app.model.dto.post.BasePostSimpleDTO;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.Tag;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.service.PostService;
import run.halo.app.service.PostTagService;
import run.halo.app.service.TagService;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Portal tag controller.
 *
 * @author johnniang
 * @author ryanwang
 * @date 4/2/19
 */
@RestController("ApiContentTagController")
@RequestMapping("/api/content/tags")
public class TagController {

    private final TagService tagService;

    private final PostTagService postTagService;

    private final PostService postService;

    public TagController(TagService tagService,
                         PostTagService postTagService,
                         PostService postService) {
        this.tagService = tagService;
        this.postTagService = postTagService;
        this.postService = postService;
    }

    @GetMapping
    @ApiOperation("Lists tags")
    public List<? extends TagDTO> listTags(@SortDefault(sort = "updateTime", direction = DESC) Sort sort,
                                           @ApiParam("If the param is true, post count of tag will be returned")
                                           @RequestParam(name = "more", required = false, defaultValue = "false") Boolean more) {
        if (more) {
            return postTagService.listTagWithCountDtos(sort);
        }
        return tagService.convertTo(tagService.listAll(sort));
    }

    @GetMapping("{slugName}/posts")
    @ApiOperation("Lists posts by tag slug name")
    public Page<BasePostSimpleDTO> listPostsBy(@PathVariable("slugName") String slugName,
                                               @PageableDefault(sort = "updateTime", direction = DESC) Pageable pageable) {
        // Get tag by slug name
        Tag tag = tagService.getBySlugNameOfNonNull(slugName);

        // Get posts, convert and return
        Page<Post> postPage = postTagService.pagePostsBy(tag.getId(), PostStatus.PUBLISHED, pageable);
        return postService.convertToSimple(postPage);
    }
}
