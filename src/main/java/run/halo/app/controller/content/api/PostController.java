package run.halo.app.controller.content.api;

import static org.springframework.data.domain.Sort.Direction.DESC;

import io.swagger.annotations.ApiOperation;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;
import run.halo.app.cache.lock.CacheLock;
import run.halo.app.cache.lock.CacheParam;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.dto.BaseCommentDTO;
import run.halo.app.model.dto.post.BasePostSimpleDTO;
import run.halo.app.model.entity.Content;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.PostComment;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.params.PostCommentParam;
import run.halo.app.model.params.PostQuery;
import run.halo.app.model.vo.BaseCommentVO;
import run.halo.app.model.vo.BaseCommentWithParentVO;
import run.halo.app.model.vo.CommentWithHasChildrenVO;
import run.halo.app.model.vo.PostDetailVO;
import run.halo.app.model.vo.PostListVO;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostCommentService;
import run.halo.app.service.PostService;

/**
 * Content post controller.
 *
 * @author johnniang
 * @author ryanwang
 * @author guqing
 * @date 2019-04-02
 */
@RestController("ApiContentPostController")
@RequestMapping("/api/content/posts")
public class PostController {

    private final PostService postService;

    private final PostCommentService postCommentService;

    private final OptionService optionService;

    public PostController(PostService postService,
        PostCommentService postCommentService,
        OptionService optionService) {
        this.postService = postService;
        this.postCommentService = postCommentService;
        this.optionService = optionService;
    }

    //CS304 issue for https://github.com/halo-dev/halo/issues/1351

    /**
     * Enable users search published articles with keywords
     *
     * @param pageable store the priority of the sort algorithm
     * @param keyword search articles with keyword
     * @param categoryId search articles with categoryId
     * @return published articles that contains keywords and specific categoryId
     */
    @GetMapping
    @ApiOperation("Lists posts")
    public Page<PostListVO> pageBy(
        @PageableDefault(sort = {"topPriority", "createTime"}, direction = DESC) Pageable pageable,
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "categoryId", required = false) Integer categoryId) {
        PostQuery postQuery = new PostQuery();
        postQuery.setKeyword(keyword);
        postQuery.setCategoryId(categoryId);
        postQuery.setStatuses(Set.of(PostStatus.PUBLISHED));
        Page<Post> postPage = postService.pageBy(postQuery, pageable);
        return postService.convertToListVo(postPage, true);
    }

    @PostMapping(value = "search")
    @ApiOperation("Lists posts by keyword")
    public Page<BasePostSimpleDTO> pageBy(@RequestParam(value = "keyword") String keyword,
        @PageableDefault(sort = "createTime", direction = DESC) Pageable pageable) {
        Page<Post> postPage = postService.pageBy(keyword, pageable);
        return postService.convertToSimple(postPage);
    }

    @GetMapping("{postId:\\d+}")
    @ApiOperation("Gets a post")
    public PostDetailVO getBy(@PathVariable("postId") Integer postId,
        @RequestParam(value = "formatDisabled", required = false, defaultValue = "true")
            Boolean formatDisabled,
        @RequestParam(value = "sourceDisabled", required = false, defaultValue = "false")
            Boolean sourceDisabled) {
        Post post = postService.getById(postId);
        post.setContent(Content.PatchedContent.of(postService.getContentById(postId)));
        PostDetailVO postDetailVO = postService.convertToDetailVo(post);

        if (formatDisabled) {
            // Clear the format content
            postDetailVO.setContent(null);
        }

        if (sourceDisabled) {
            // Clear the original content
            postDetailVO.setOriginalContent(null);
        }

        postService.publishVisitEvent(postDetailVO.getId());

        return postDetailVO;
    }

    @GetMapping("/slug")
    @ApiOperation("Gets a post")
    public PostDetailVO getBy(@RequestParam("slug") String slug,
        @RequestParam(value = "formatDisabled", required = false, defaultValue = "true")
            Boolean formatDisabled,
        @RequestParam(value = "sourceDisabled", required = false, defaultValue = "false")
            Boolean sourceDisabled) {
        Post post = postService.getBySlug(slug);
        post.setContent(Content.PatchedContent.of(postService.getContentById(post.getId())));
        PostDetailVO postDetailVO = postService.convertToDetailVo(post);

        if (formatDisabled) {
            // Clear the format content
            postDetailVO.setContent(null);
        }

        if (sourceDisabled) {
            // Clear the original content
            postDetailVO.setOriginalContent(null);
        }

        postService.publishVisitEvent(postDetailVO.getId());

        return postDetailVO;
    }

    @GetMapping("{postId:\\d+}/prev")
    @ApiOperation("Gets previous post by current post id.")
    public PostDetailVO getPrevPostBy(@PathVariable("postId") Integer postId) {
        Post post = postService.getById(postId);
        Post prevPost =
            postService.getPrevPost(post).orElseThrow(() -> new NotFoundException("查询不到该文章的信息"));
        prevPost.setContent(Content.PatchedContent.of(postService.getContentById(prevPost.getId())));
        return postService.convertToDetailVo(prevPost);
    }

    @GetMapping("{postId:\\d+}/next")
    @ApiOperation("Gets next post by current post id.")
    public PostDetailVO getNextPostBy(@PathVariable("postId") Integer postId) {
        Post post = postService.getById(postId);
        Post nextPost =
            postService.getNextPost(post).orElseThrow(() -> new NotFoundException("查询不到该文章的信息"));
        nextPost.setContent(Content.PatchedContent.of(postService.getContentById(nextPost.getId())));
        return postService.convertToDetailVo(nextPost);
    }

    @GetMapping("{postId:\\d+}/comments/top_view")
    public Page<CommentWithHasChildrenVO> listTopComments(@PathVariable("postId") Integer postId,
        @RequestParam(name = "page", required = false, defaultValue = "0") int page,
        @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        return postCommentService.pageTopCommentsBy(postId, CommentStatus.PUBLISHED,
            PageRequest.of(page, optionService.getCommentPageSize(), sort));
    }

    @GetMapping("{postId:\\d+}/comments/{commentParentId:\\d+}/children")
    public List<BaseCommentDTO> listChildrenBy(@PathVariable("postId") Integer postId,
        @PathVariable("commentParentId") Long commentParentId,
        @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        // Find all children comments
        List<PostComment> postComments = postCommentService
            .listChildrenBy(postId, commentParentId, CommentStatus.PUBLISHED, sort);
        // Convert to base comment dto

        return postCommentService.convertTo(postComments);
    }

    @GetMapping("{postId:\\d+}/comments/tree_view")
    @ApiOperation("Lists comments with tree view")
    public Page<BaseCommentVO> listCommentsTree(@PathVariable("postId") Integer postId,
        @RequestParam(name = "page", required = false, defaultValue = "0") int page,
        @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        return postCommentService
            .pageVosBy(postId, PageRequest.of(page, optionService.getCommentPageSize(), sort));
    }

    @GetMapping("{postId:\\d+}/comments/list_view")
    @ApiOperation("Lists comment with list view")
    public Page<BaseCommentWithParentVO> listComments(@PathVariable("postId") Integer postId,
        @RequestParam(name = "page", required = false, defaultValue = "0") int page,
        @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        return postCommentService.pageWithParentVoBy(postId,
            PageRequest.of(page, optionService.getCommentPageSize(), sort));
    }

    @PostMapping("comments")
    @ApiOperation("Comments a post")
    @CacheLock(autoDelete = false, traceRequest = true)
    public BaseCommentDTO comment(@RequestBody PostCommentParam postCommentParam) {
        postCommentService.validateCommentBlackListStatus();

        // Escape content
        postCommentParam.setContent(HtmlUtils
            .htmlEscape(postCommentParam.getContent(), StandardCharsets.UTF_8.displayName()));
        return postCommentService.convertTo(postCommentService.createBy(postCommentParam));
    }

    @PostMapping("{postId:\\d+}/likes")
    @ApiOperation("Likes a post")
    @CacheLock(autoDelete = false, traceRequest = true)
    public void like(@PathVariable("postId") @CacheParam Integer postId) {
        postService.increaseLike(postId);
    }
}
