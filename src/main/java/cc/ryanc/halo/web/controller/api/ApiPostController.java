package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.exception.NotFoundException;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.support.JsonResult;
import cc.ryanc.halo.model.enums.PostStatusEnum;
import cc.ryanc.halo.model.enums.PostTypeEnum;
import cc.ryanc.halo.service.CategoryService;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.service.TagService;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static cc.ryanc.halo.utils.HaloUtils.getDefaultPageSize;
import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * <pre>
 *     文章API
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/6/6
 */
@RestController
@RequestMapping(value = "/api/posts")
public class ApiPostController {

    @Autowired
    private PostService postService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TagService tagService;

    /**
     * 获取文章列表 分页
     *
     * <p>
     * result api
     * <pre>
     * {
     *     "code": 200,
     *     "msg": "OK",
     *     "result": {
     *         "content": [
     *             {
     *                 "postId": ,
     *                 "user": {},
     *                 "postTitle": "",
     *                 "postType": "",
     *                 "postContentMd": "",
     *                 "postContent": "",
     *                 "postUrl": "",
     *                 "postSummary": "",
     *                 "categories": [],
     *                 "tags": [],
     *                 "comments": [],
     *                 "postThumbnail": "",
     *                 "postDate": "",
     *                 "postUpdate": "",
     *                 "postStatus": 0,
     *                 "postViews": 0,
     *                 "allowComment": 1,
     *                 "customTpl": ""
     *             }
     *         ],
     *         "pageable": {
     *             "sort": {
     *                 "sorted": true,
     *                 "unsorted": false,
     *                 "empty": false
     *             },
     *             "offset": 0,
     *             "pageSize": 10,
     *             "pageNumber": 0,
     *             "unpaged": false,
     *             "paged": true
     *         },
     *         "last": true,
     *         "totalElements": 1,
     *         "totalPages": 1,
     *         "size": 10,
     *         "number": 0,
     *         "first": true,
     *         "numberOfElements": 1,
     *         "sort": {
     *             "sorted": true,
     *             "unsorted": false,
     *             "empty": false
     *         },
     *         "empty": false
     *     }
     * }
     *     </pre>
     * </p>
     *
     * @param page 页码
     * @return JsonResult
     */
    @GetMapping(value = "/page/{page}")
    public JsonResult posts(@PathVariable(value = "page") Integer page,
                            @SortDefault(sort = "postDate", direction = DESC) Sort sort,
                            @RequestParam(value = "cateUrl", required = false) String cateUrl,
                            @RequestParam(value = "tagUrl", required = false) String tagUrl) {
        // Build page info
        Pageable pageable = PageRequest.of(page - 1, getDefaultPageSize(), sort);

        Page<Post> postPage;

        if (StrUtil.isNotBlank(cateUrl)) {
            // Query by category url
            postPage = postService.findPostByCategories(categoryService.findByCateUrl(cateUrl), pageable);
        } else if (StrUtil.isNotBlank(tagUrl)) {
            // Query by tag url
            postPage = postService.findPostsByTags(tagService.findByTagUrl(tagUrl), pageable);
        } else {
            // Query default
            postPage = postService.findPostByStatus(PostStatusEnum.PUBLISHED.getCode(), PostTypeEnum.POST_TYPE_POST.getDesc(), pageable);
        }

        return JsonResult.ok(HttpStatus.OK.getReasonPhrase(), postPage);
    }

    /**
     * 获取单个文章信息
     *
     * <p>
     * result json:
     * <pre>
     * {
     *     "code": 200,
     *     "msg": "OK",
     *     "result": {
     *         "postId": ,
     *         "user": {},
     *         "postTitle": "",
     *         "postType": "",
     *         "postContentMd": "",
     *         "postContent": "",
     *         "postUrl": "",
     *         "postSummary": "",
     *         "categories": [],
     *         "tags": [],
     *         "comments": [],
     *         "postThumbnail": "",
     *         "postDate": "",
     *         "postUpdate": "",
     *         "postStatus": 0,
     *         "postViews": 0,
     *         "allowComment": 1,
     *         "customTpl": ""
     *     }
     * }
     *     </pre>
     * </p>
     *
     * @param postId 文章编号
     * @return JsonResult
     */
    @GetMapping(value = "/{postId}")
    public Post posts(@PathVariable(value = "postId") Long postId) {
        // Find post by post id
        Post post = Optional.ofNullable(postService.findByPostId(postId, PostTypeEnum.POST_TYPE_POST.getDesc()))
                .orElseThrow(() -> new NotFoundException("Post with id: " + postId + " was not found").setErrorData(postId));

        // Cache views
        postService.cacheViews(post.getPostId());

        return post;
    }

}
