package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.model.domain.Tag;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping(value = "/api/tags")
public class ApiTagController {

    @Autowired
    private TagService tagService;

    /**
     * 获取所有标签
     *
     * @return JsonResult
     */
    @GetMapping
    public JsonResult tags(){
        List<Tag> tags = tagService.findAllTags();
        if(null!=tags && tags.size()>0){
            return new JsonResult(200,"success",tags);
        }else{
            return new JsonResult(200,"empty");
        }
    }

    /**
     * 获取单个标签的信息
     *
     * @param tagUrl tagUrl
     * @return JsonResult
     */
    @GetMapping(value = "/${tagUrl}")
    public JsonResult tags(@PathVariable("tagUrl") String tagUrl){
        Tag tag = tagService.findByTagUrl(tagUrl);
        if(null!=tag){
            return new JsonResult(200,"success",tag);
        }else{
            return new JsonResult(404,"not found");
        }
    }
}
