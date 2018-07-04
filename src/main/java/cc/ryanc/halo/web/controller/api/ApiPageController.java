package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.enums.PostType;
import cc.ryanc.halo.model.enums.ResponseStatus;
import cc.ryanc.halo.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
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
     * @return JsonResult
     */
    @GetMapping(value = "/{postUrl}")
    public JsonResult pages(@PathVariable(value = "postUrl") String postUrl){
        Post post = postService.findByPostUrl(postUrl,PostType.POST_TYPE_PAGE.getDesc());
        if(null!=post){
            return new JsonResult(ResponseStatus.SUCCESS.getCode(),ResponseStatus.SUCCESS.getMsg(),post);
        }else{
            return new JsonResult(ResponseStatus.NOTFOUND.getCode(),ResponseStatus.NOTFOUND.getMsg());
        }
    }
}
