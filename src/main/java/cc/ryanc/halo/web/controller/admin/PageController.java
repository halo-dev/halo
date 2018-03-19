package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.Link;
import cc.ryanc.halo.service.LinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.websocket.server.PathParam;
import java.util.List;

/**
 * @author : RYAN0UP
 * @date : 2017/12/10
 * @version : 1.0
 * description : 预设页面，自定义页面
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/page")
public class PageController {

    @Autowired
    private LinkService linkService;

    @GetMapping
    public String pages(){
        return "admin/admin_page";
    }

    /**
     * 获取友情链接列表并渲染页面
     *
     * @param map ModelMap
     * @return String
     */
    @GetMapping(value = "/links")
    public String links(Model model){
        List<Link> links = linkService.findAllLinks();
        model.addAttribute("links",links);
        return "admin/admin_link";
    }

    /**
     * 跳转到修改页面
     *
     * @param model model
     * @param linkId linkId
     * @return String
     */
    @GetMapping("/links/edit")
    public String toEditLink(Model model,@PathParam("linkId") Long linkId){
        Link link = linkService.findByLinkId(linkId);
        model.addAttribute("link",link);
        return "admin/admin_link-update";
    }

    /**
     * 处理添加友链的请求并渲染页面
     *
     * @param link Link
     * @return freemarker
     */
    @PostMapping(value = "/links/save")
    public String saveLink(@ModelAttribute Link link){
        try{
            Link backLink = linkService.saveByLink(link);
            log.info("保存成功，数据为："+backLink);
        }catch (Exception e){
            log.error("未知错误："+e.getMessage());
        }
        return "redirect:/admin/page/links";
    }

    /**
     * 处理删除友情链接的请求并重定向
     *
     * @param linkId linkId
     * @return String
     */
    @GetMapping(value = "/links/remove")
    public String removeLink(@PathParam("linkId") Long linkId){
        try{
            Link link = linkService.removeByLinkId(linkId);
            log.info("删除的友情链接："+link);
        }catch (Exception e){
            log.error("未知错误："+e.getMessage());
        }
        return "redirect:/admin/page/links";
    }

    /**
     * 处理修改的请求并重定向
     *
     * @param link Link
     * @return freemarker
     */
    @PostMapping(value = "/links/update")
    public String updateLink(@ModelAttribute Link link){
        try {
            Link beforeLink = linkService.findByLinkId(link.getLinkId());
            linkService.updateByLink(link);
            log.info("修改友情链接页面：修改之前的数据："+beforeLink+"，修改之后的数据："+link);
        }catch (Exception e){
            log.error("未知错误："+e.getMessage());
        }
        return "redirect:/admin/page/links";
    }

    /**
     * 图库管理
     *
     * @return String
     */
    @GetMapping(value = "/gallery")
    public String gallery(){
        return "";
    }
}
