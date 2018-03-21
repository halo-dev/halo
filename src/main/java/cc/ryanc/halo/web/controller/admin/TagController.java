package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.Tag;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.List;
import java.util.Optional;

/**
 * @author : RYAN0UP
 * @date : 2017/12/10
 * @version : 1.0
 * description: 标签控制器
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/tag")
public class TagController {

    @Autowired
    private TagService tagService;

    /**
     * 渲染标签管理页面
     *
     * @param model model
     * @return string
     */
    @GetMapping
    public String tags(Model model){
        List<Tag> tags = tagService.findAllTags();
        model.addAttribute("tags",tags);
        //设置选项
        model.addAttribute("options",HaloConst.OPTIONS);
        return "admin/admin_tag";
    }

    /**
     * 新增标签
     *
     * @param tag tag
     * @return string
     */
    @PostMapping(value = "/save")
    public String saveTag(@ModelAttribute Tag tag){
        try{
            Tag backTag = tagService.saveByTag(tag);
            log.info("新添加的标签为："+backTag);
        }catch (Exception e){
            log.error("未知错误："+e.getMessage());
        }
        return "redirect:/admin/tag";
    }

    /**
     * 验证是否存在该路径
     *
     * @param tagUrl tagUrl
     * @return string
     */
    @GetMapping(value = "/checkUrl")
    @ResponseBody
    public boolean checkTagUrlExists(@RequestParam("tagUrl") String tagUrl){
        Tag tag = tagService.findByTagUrl(tagUrl);
        if(null!=tag){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 处理删除标签的请求
     *
     * @param tagId tagId
     * @return string
     */
    @GetMapping(value = "/remove")
    public String removeTag(@PathParam("tagId") Long tagId){
        try{
            Tag tag = tagService.removeByTagId(tagId);
            log.info("删除的标签："+tag);
        }catch (Exception e){
            log.error("未知错误："+e.getMessage());
        }
        return "redirect:/admin/tag";
    }

    /**
     * 跳转到修改标签页面
     *
     * @param model model
     * @param tagId tagId
     * @return string
     */
    @GetMapping(value = "/edit")
    public String toEditTag(Model model,@PathParam("tagId") Long tagId){
        try{
            Optional<Tag> tag = tagService.findByTagId(tagId);
            model.addAttribute("tag",tag.get());
            //设置选项
            model.addAttribute("options",HaloConst.OPTIONS);
            log.info("tagId为"+tagId+"的数据为："+tag.get());
        }catch (Exception e){
            log.error("未知错误："+e.getMessage());
        }
        return "admin/admin_tag-update";
    }

    /**
     * 处理修改标签的请求
     *
     * @param tag tag
     * @return string
     */
    @PostMapping(value = "/update")
    public String updateTag(@ModelAttribute Tag tag){
        try {
            Optional<Tag> beforeTag = tagService.findByTagId(tag.getTagId());
            log.info("修改之前的数据："+beforeTag.get()+"，修改之后的数据："+tag);
            tagService.updateByTag(tag);
        }catch (Exception e){
            log.error("未知错误："+e.getMessage());
        }
        return "redirect:/admin/tag";
    }
}
