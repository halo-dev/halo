package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.model.dto.Archive;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.enums.ResponseStatus;
import cc.ryanc.halo.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
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
     * @return JsonResult
     */
    @GetMapping(value = "/year")
    public JsonResult archivesYear(){
        List<Archive> archives = postService.findPostGroupByYear();
        if(null!=archives || archives.size()>0){
            return new JsonResult(ResponseStatus.SUCCESS.getCode(),ResponseStatus.SUCCESS.getMsg(),archives);
        }else {
            return new JsonResult(ResponseStatus.EMPTY.getCode(),ResponseStatus.EMPTY.getMsg());
        }
    }

    /**
     * 根据月份归档
     *
     * @return JsonResult
     */
    @GetMapping(value = "/year/month")
    public JsonResult archivesYearAndMonth(){
        List<Archive> archives = postService.findPostGroupByYearAndMonth();
        if(null!=archives || archives.size()>0){
            return new JsonResult(ResponseStatus.SUCCESS.getCode(),ResponseStatus.SUCCESS.getMsg(),archives);
        }else {
            return new JsonResult(ResponseStatus.EMPTY.getCode(),ResponseStatus.EMPTY.getMsg());
        }
    }
}
