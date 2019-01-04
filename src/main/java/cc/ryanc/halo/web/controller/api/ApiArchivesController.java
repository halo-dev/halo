package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.model.dto.Archive;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.enums.ResponseStatusEnum;
import cc.ryanc.halo.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@CrossOrigin
@RestController
@RequestMapping(value = "/api/archives")
public class ApiArchivesController {

    @Autowired
    private PostService postService;

    /**
     * 根据年份归档
     *
     * <p>
     *     result json:
     *     <pre>
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
        if (null != archives && archives.size() > 0) {
            return new JsonResult(ResponseStatusEnum.SUCCESS.getCode(), ResponseStatusEnum.SUCCESS.getMsg(), archives);
        } else {
            return new JsonResult(ResponseStatusEnum.EMPTY.getCode(), ResponseStatusEnum.EMPTY.getMsg());
        }
    }

    /**
     * 根据月份归档
     *
     * <p>
     *     result json:
     *     <pre>
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
    public JsonResult archivesYearAndMonth() {
        final List<Archive> archives = postService.findPostGroupByYearAndMonth();
        if (null != archives && archives.size() > 0) {
            return new JsonResult(ResponseStatusEnum.SUCCESS.getCode(), ResponseStatusEnum.SUCCESS.getMsg(), archives);
        } else {
            return new JsonResult(ResponseStatusEnum.EMPTY.getCode(), ResponseStatusEnum.EMPTY.getMsg());
        }
    }

    /**
     * @Author Aquan
     * @Description 返回所有文章
     * @Date 2019.1.4 11:06
     * @Param
     * @return JsonResult
     **/
    @GetMapping(value = "/all")
    public JsonResult archivesAllPost() {
        final List<Archive> archive = postService.findAllPost();
        if (null != archive && archive.size() > 0) {
            return new JsonResult(ResponseStatusEnum.SUCCESS.getCode(), ResponseStatusEnum.SUCCESS.getMsg(), archive);
        } else {
            return new JsonResult(ResponseStatusEnum.EMPTY.getCode(), ResponseStatusEnum.EMPTY.getMsg());
        }
    }


}
