package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.JsonResult;
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
        Post post = postService.findByPostUrl(postUrl,HaloConst.POST_TYPE_PAGE);
        if(null!=post){
            return new JsonResult(200,"success",post);
        }else{
            return new JsonResult(404,"not found");
        }
    }
}
