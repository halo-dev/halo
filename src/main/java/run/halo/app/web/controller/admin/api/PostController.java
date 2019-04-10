package run.halo.app.web.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.dto.post.PostMinimalOutputDTO;
import run.halo.app.model.dto.post.PostSimpleOutputDTO;
import run.halo.app.model.entity.Post;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.params.PostParam;
import run.halo.app.model.params.PostQuery;
import run.halo.app.model.vo.PostDetailVO;
import run.halo.app.service.*;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Post controller.
 *
 * @author johnniang
 * @date 3/19/19
 */
@RestController
@RequestMapping("/admin/api/posts")
public class PostController {

    private final PostService postService;

    private final PostCategoryService postCategoryService;

    private final PostTagService postTagService;

    private final CommentService commentService;

    private final OptionService optionService;

    public PostController(PostService postService,
                          PostCategoryService postCategoryService,
                          PostTagService postTagService,
                          CommentService commentService,
                          OptionService optionService) {
        this.postService = postService;
        this.postCategoryService = postCategoryService;
        this.postTagService = postTagService;
        this.commentService = commentService;
        this.optionService = optionService;
    }

    @GetMapping
    @ApiOperation("Lists posts")
    public Page<PostSimpleOutputDTO> pageBy(@PageableDefault(sort = "updateTime", direction = DESC) Pageable pageable,
                                            PostQuery postQuery) {
        Page<Post> postPage = postService.pageBy(postQuery, pageable);
        return postService.convertTo(postPage);
    }

    @GetMapping("latest")
    @ApiOperation("Pages latest post")
    public List<PostMinimalOutputDTO> pageLatest(@RequestParam(name = "top", defaultValue = "10") int top) {
        return postService.pageLatestOfMinimal(top).getContent();
    }

    @GetMapping("status/{status}")
    @ApiOperation("Gets a page of post by post status")
    public Page<? extends PostSimpleOutputDTO> pageByStatus(@PathVariable(name = "status") PostStatus status,
                                                            @RequestParam(value = "more", required = false, defaultValue = "false") Boolean more,
                                                            @PageableDefault(sort = "updateTime", direction = DESC) Pageable pageable) {
        if (more) {
            return postService.pageListVoBy(status, pageable);
        }
        return postService.pageSimpleDtoByStatus(status, pageable);
    }

    @GetMapping("{postId:\\d+}")
    public PostDetailVO getBy(@PathVariable("postId") Integer postId) {
        return postService.getDetailVoBy(postId);
    }

    @PostMapping
    public PostDetailVO createBy(@Valid @RequestBody PostParam postParam) {
        // Convert to
        Post post = postParam.convertTo();

        return postService.createBy(post, postParam.getTagIds(), postParam.getCategoryIds());
    }

    @PutMapping("{postId:\\d+}")
    public PostDetailVO updateBy(@Valid @RequestBody PostParam postParam,
                                 @PathVariable("postId") Integer postId) {
        // Get the post info
        Post postToUpdate = postService.getById(postId);

        postParam.update(postToUpdate);

        return postService.updateBy(postToUpdate, postParam.getTagIds(), postParam.getCategoryIds());
    }

    @DeleteMapping("{postId:\\d+}")
    public void deletePermanently(@PathVariable("postId") Integer postId) {
        // Remove it
        postService.removeById(postId);
        postCategoryService.removeByPostId(postId);
        postTagService.removeByPostId(postId);
    }

}
