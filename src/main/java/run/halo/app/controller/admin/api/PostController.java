package run.halo.app.controller.admin.api;

import cn.hutool.core.util.IdUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.model.dto.post.BasePostDetailDTO;
import run.halo.app.model.dto.post.BasePostMinimalDTO;
import run.halo.app.model.dto.post.BasePostSimpleDTO;
import run.halo.app.model.entity.Post;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.params.PostContentParam;
import run.halo.app.model.params.PostParam;
import run.halo.app.model.params.PostQuery;
import run.halo.app.model.params.PostStatusUpdateParam;
import run.halo.app.model.vo.PostDetailVO;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostService;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Post controller.
 *
 * @author johnniang
 * @author ryanwang
 * @author guqing
 * @date 3/19/19
 */
@RestController
@RequestMapping("/api/admin/posts")
public class PostController {

    private final PostService postService;

    private final StringCacheStore cacheStore;

    private final OptionService optionService;

    public PostController(PostService postService,
                          StringCacheStore cacheStore,
                          OptionService optionService) {
        this.postService = postService;
        this.cacheStore = cacheStore;
        this.optionService = optionService;
    }

    @GetMapping
    @ApiOperation("Lists posts")
    public Page<? extends BasePostSimpleDTO> pageBy(@PageableDefault(sort = {"topPriority", "createTime"}, direction = DESC) Pageable pageable,
                                                    PostQuery postQuery,
                                                    @RequestParam(value = "more", defaultValue = "true") Boolean more) {
        Page<Post> postPage = postService.pageBy(postQuery, pageable);
        if (more) {
            return postService.convertToListVo(postPage);
        }

        return postService.convertToSimple(postPage);
    }

    @GetMapping("latest")
    @ApiOperation("Pages latest post")
    public List<BasePostMinimalDTO> pageLatest(@RequestParam(name = "top", defaultValue = "10") int top) {
        return postService.convertToMinimal(postService.pageLatest(top).getContent());
    }

    @GetMapping("status/{status}")
    @ApiOperation("Gets a page of post by post status")
    public Page<? extends BasePostSimpleDTO> pageByStatus(@PathVariable(name = "status") PostStatus status,
                                                          @RequestParam(value = "more", required = false, defaultValue = "false") Boolean more,
                                                          @PageableDefault(sort = "createTime", direction = DESC) Pageable pageable) {
        Page<Post> posts = postService.pageBy(status, pageable);

        if (more) {
            return postService.convertToListVo(posts);
        }

        return postService.convertToSimple(posts);
    }

    @GetMapping("{postId:\\d+}")
    public PostDetailVO getBy(@PathVariable("postId") Integer postId) {
        Post post = postService.getById(postId);
        return postService.convertToDetailVo(post);
    }

    @PutMapping("{postId:\\d+}/likes")
    @ApiOperation("Likes a post")
    public void likes(@PathVariable("postId") Integer postId) {
        postService.increaseLike(postId);
    }

    @PostMapping
    public PostDetailVO createBy(@Valid @RequestBody PostParam postParam,
                                 @RequestParam(value = "autoSave", required = false, defaultValue = "false") Boolean autoSave) {
        // Convert to
        Post post = postParam.convertTo();
        return postService.createBy(post, postParam.getTagIds(), postParam.getCategoryIds(), postParam.getPostMetas(), autoSave);
    }

    @PutMapping("{postId:\\d+}")
    public PostDetailVO updateBy(@Valid @RequestBody PostParam postParam,
                                 @PathVariable("postId") Integer postId,
                                 @RequestParam(value = "autoSave", required = false, defaultValue = "false") Boolean autoSave) {
        // Get the post info
        Post postToUpdate = postService.getById(postId);

        postParam.update(postToUpdate);
        return postService.updateBy(postToUpdate, postParam.getTagIds(), postParam.getCategoryIds(), postParam.getPostMetas(), autoSave);
    }

    @PutMapping("{postId:\\d+}/status/{status}")
    @ApiOperation("Update post status")
    public BasePostMinimalDTO updateStatusBy(
            @PathVariable("postId") Integer postId,
            @PathVariable("status") PostStatus status) {
        Post post = postService.updateStatus(status, postId);

        return new BasePostMinimalDTO().convertFrom(post);
    }

    @PutMapping("status")
    @ApiOperation("Update post status in batch")
    public List<Post> updateStatusInBatch(@RequestBody PostStatusUpdateParam param) {
        return postService.updateStatusByIds(param.getIds(), param.getStatus());
    }

    @PutMapping("{postId:\\d+}/status/draft/content")
    @ApiOperation("Update draft")
    public BasePostDetailDTO updateDraftBy(
            @PathVariable("postId") Integer postId,
            @RequestBody PostContentParam contentParam) {
        // Update draft content
        Post post = postService.updateDraftContent(contentParam.getContent(), postId);

        return new BasePostDetailDTO().convertFrom(post);
    }

    @DeleteMapping("{postId:\\d+}")
    public void deletePermanently(@PathVariable("postId") Integer postId) {
        postService.removeById(postId);
    }

    @DeleteMapping
    @ApiOperation("Delete posts permanently in batch by id array")
    public List<Post> deletePermanentlyInBatch(@RequestBody List<Integer> ids) {
        return postService.removeByIds(ids);
    }

    @GetMapping(value = {"preview/{postId:\\d+}", "{postId:\\d+}/preview"})
    @ApiOperation("Get preview link")
    public String preview(@PathVariable("postId") Integer postId) {
        Post post = postService.getById(postId);

        String token = IdUtil.simpleUUID();

        // cache preview token
        cacheStore.putAny("preview-post-token-" + postId, token, 10, TimeUnit.MINUTES);

        // build preview post url and return
        return String.format("%s/archives/%s?preview=true&token=%s", optionService.getBlogBaseUrl(), post.getUrl(), token);
    }
}
