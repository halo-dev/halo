package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.exception.NotFoundException;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.enums.PostTypeEnum;
import cc.ryanc.halo.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <pre>
 *     页面API
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/6/6
 */
@RestController
@RequestMapping(value = "/api/pages")
public class ApiPageController {

    @Autowired
    private PostService postService;

    /**
     * 获取单个页面
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
     *         "postSummary": ,
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
     * @param postId postId
     *
     * @return JsonResult
     */
    @GetMapping(value = "/{postId}")
    public Post pages(@PathVariable(value = "postId") Long postId) {
        final Post post = postService.findByPostId(postId, PostTypeEnum.POST_TYPE_PAGE.getDesc());
        if (post == null) {
            throw new NotFoundException("Post with id: " + postId + " was not found").setErrorData(postId);
        }

        // Cache views
        postService.cacheViews(postId);

        return post;
    }
}
