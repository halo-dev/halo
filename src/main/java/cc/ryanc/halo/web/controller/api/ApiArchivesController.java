package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.model.dto.Archive;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <pre>
 *     文章归档API
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/6/6
 */
@RestController
@RequestMapping(value = "/api/archives")
public class ApiArchivesController {

    @Autowired
    private PostService postService;

    /**
     * 根据年份归档
     *
     * <p>
     * result json:
     * <pre>
     * {
     *     "code": 200,
     *     "msg": "OK",
     *     "result": [
     *         {
     *             "year": "",
     *             "month": "",
     *             "count": "",
     *             "posts": [
     *                 {
     *                     "postId": "",
     *                     "user": {},
     *                     "postTitle": "",
     *                     "postType": "",
     *                     "postContentMd": "",
     *                     "postContent": "",
     *                     "postUrl": "",
     *                     "postSummary": "",
     *                     "categories": [],
     *                     "tags": [],
     *                     "comments": [],
     *                     "postThumbnail": "",
     *                     "postDate": "",
     *                     "postUpdate": "",
     *                     "postStatus": 0,
     *                     "postViews": 0,
     *                     "allowComment": 1,
     *                     "customTpl": ""
     *                 }
     *             ]
     *         }
     *     ]
     * }
     *     </pre>
     * </p>
     *
     * @return JsonResult
     */
    @GetMapping(value = "/year")
    public JsonResult archivesYear() {
        final List<Archive> archives = postService.findPostGroupByYear();
        if (!CollectionUtils.isEmpty(archives)) {
            return new JsonResult(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), archives);
        } else {
            return new JsonResult(HttpStatus.NO_CONTENT.value(), HttpStatus.NO_CONTENT.getReasonPhrase());
        }
    }

    /**
     * 根据月份归档
     *
     * <p>
     * result json:
     * <pre>
     * {
     *     "code": 200,
     *     "msg": "OK",
     *     "result": [
     *         {
     *             "year": "",
     *             "month": "",
     *             "count": "",
     *             "posts": [
     *                 {
     *                     "postId": "",
     *                     "user": {},
     *                     "postTitle": "",
     *                     "postType": "",
     *                     "postContentMd": "",
     *                     "postContent": "",
     *                     "postUrl": "",
     *                     "postSummary": "",
     *                     "categories": [],
     *                     "tags": [],
     *                     "comments": [],
     *                     "postThumbnail": "",
     *                     "postDate": "",
     *                     "postUpdate": "",
     *                     "postStatus": 0,
     *                     "postViews": 0,
     *                     "allowComment": 1,
     *                     "customTpl": ""
     *                 }
     *             ]
     *         }
     *     ]
     * }
     *     </pre>
     * </p>
     *
     * @return JsonResult
     */
    @GetMapping(value = "/year/month")
    public List<Archive> archivesYearAndMonth() {
        return postService.findPostGroupByYearAndMonth();
    }

    /**
     * @return JsonResult
     *
     * @Author Aquan
     * @Description 返回所有文章
     * @Date 2019.1.4 11:06
     * @Param
     **/
    @GetMapping(value = "/all")
    public List<Archive> archivesAllPost() {
        return postService.findAllPost();
    }


}
