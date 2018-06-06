package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.service.PostService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : RYAN0UP
 * @date : 2018/6/6
 */
@RestController
@RequestMapping(value = "/api/posts")
public class ApiPostController {

    @Autowired
    private PostService postService;

    /**
     * 获取文章列表 分页
     *
     * @param page 页码
     * @return JsonResult
     */
    @GetMapping(value = "/posts/{page}")
    public JsonResult posts(@PathVariable(value = "page") Integer page){
        Sort sort = new Sort(Sort.Direction.DESC, "postDate");
        Integer size = 10;
        if (!StringUtils.isBlank(HaloConst.OPTIONS.get("index_posts"))) {
            size = Integer.parseInt(HaloConst.OPTIONS.get("index_posts"));
        }
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Post> posts = postService.findPostByStatus(0, HaloConst.POST_TYPE_POST, pageable);
        if (null == posts) {
            return new JsonResult(200,"empty");
        }
        return new JsonResult(200,"success",posts);
    }

    /**
     * 获取单个文章信息
     *
     * @param postUrl 文章路径
     * @return JsonResult
     */
    @GetMapping(value = "/posts/{postUrl}")
    public JsonResult posts(@PathVariable(value = "postUrl") String postUrl){
        Post post = postService.findByPostUrl(postUrl,HaloConst.POST_TYPE_POST);
        if(null!=post){
            return new JsonResult(200,"success",post);
        }else {
            return new JsonResult(404,"not found");
        }
    }
}
