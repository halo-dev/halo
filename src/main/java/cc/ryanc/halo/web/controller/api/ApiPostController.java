package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.enums.PostStatus;
import cc.ryanc.halo.model.enums.PostType;
import cc.ryanc.halo.model.enums.ResponseStatus;
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

import java.util.List;

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
    @GetMapping(value = "/page/{page}")
    public JsonResult posts(@PathVariable(value = "page") Integer page){
        Sort sort = new Sort(Sort.Direction.DESC, "postDate");
        Integer size = 10;
        if (!StringUtils.isBlank(HaloConst.OPTIONS.get("index_posts"))) {
            size = Integer.parseInt(HaloConst.OPTIONS.get("index_posts"));
        }
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Post> posts = postService.findPostByStatus(PostStatus.PUBLISHED.getCode(), PostType.POST_TYPE_POST.getDesc(), pageable);
        if (null == posts) {
            return new JsonResult(ResponseStatus.EMPTY.getCode(),ResponseStatus.EMPTY.getMsg());
        }
        return new JsonResult(ResponseStatus.SUCCESS.getCode(),ResponseStatus.SUCCESS.getMsg(),posts);
    }

    @GetMapping(value = "/hot")
    public JsonResult hotPosts() {
        List<Post> posts = postService.hotPosts();
        if (null != posts && posts.size() > 0) {
            return new JsonResult(ResponseStatus.SUCCESS.getCode(),ResponseStatus.SUCCESS.getMsg(), posts);
        } else {
            return new JsonResult(ResponseStatus.EMPTY.getCode(),ResponseStatus.EMPTY.getMsg());
        }
    }

    /**
     * 获取单个文章信息
     *
     * @param postUrl 文章路径
     * @return JsonResult
     */
    @GetMapping(value = "/{postUrl}")
    public JsonResult posts(@PathVariable(value = "postUrl") String postUrl){
        Post post = postService.findByPostUrl(postUrl,PostType.POST_TYPE_POST.getDesc());
        if(null!=post){
            return new JsonResult(ResponseStatus.SUCCESS.getCode(),ResponseStatus.SUCCESS.getMsg(),post);
        }else {
            return new JsonResult(ResponseStatus.NOTFOUND.getCode(),ResponseStatus.NOTFOUND.getMsg());
        }
    }
}
