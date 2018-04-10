package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.Gallery;
import cc.ryanc.halo.model.domain.Link;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.service.GalleryService;
import cc.ryanc.halo.service.LinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
 * description : 预设页面，自定义页面
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/page")
public class PageController {

    @Autowired
    private LinkService linkService;

    @Autowired
    private GalleryService galleryService;

    /**
     * 页面管理页面
     *
     * @param model model
     * @return string
     */
    @GetMapping
    public String pages(Model model){
        model.addAttribute("options",HaloConst.OPTIONS);
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
        model.addAttribute("statusName","添加");

        //设置选项
        model.addAttribute("options", HaloConst.OPTIONS);
        return "admin/admin_page_link";
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
        List<Link> links = linkService.findAllLinks();
        Optional<Link> link = linkService.findByLinkId(linkId);
        model.addAttribute("updateLink",link.get());
        model.addAttribute("statusName","修改");
        model.addAttribute("links",links);
        //设置选项
        model.addAttribute("options",HaloConst.OPTIONS);
        return "admin/admin_page_link";
    }

    /**
     * 处理添加/修改友链的请求并渲染页面
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
            log.error("未知错误：{0}",e.getMessage());
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
            log.error("未知错误：{0}",e.getMessage());
        }
        return "redirect:/admin/page/links";
    }

    /**
     * 图库管理
     *
     * @return String
     */
    @GetMapping(value = "/galleries")
    public String gallery(Model model,
                          @RequestParam(value = "page",defaultValue = "0") Integer page,
                          @RequestParam(value = "size",defaultValue = "18") Integer size){
        Sort sort = new Sort(Sort.Direction.DESC,"galleryId");
        Pageable pageable = new PageRequest(page,size,sort);
        Page<Gallery> galleries = galleryService.findAllGalleries(pageable);
        model.addAttribute("galleries",galleries);
        model.addAttribute("options",HaloConst.OPTIONS);
        return "admin/admin_page_gallery";
    }

    /**
     * 保存图片
     *
     * @param gallery gallery
     * @return string
     */
    @PostMapping(value = "/gallery/save")
    public String saveGallery(@ModelAttribute Gallery gallery){
        try {
            if("".equals(gallery.getGalleryThumbnailUrl()) || ""==gallery.getGalleryThumbnailUrl()){
                gallery.setGalleryThumbnailUrl(gallery.getGalleryUrl());
            }
            galleryService.saveByGallery(gallery);
        }catch (Exception e){
            e.printStackTrace();
        }
        return "redirect:/admin/page/gallery";
    }

    /**
     * 处理获取图片详情的请求
     *
     * @param model model
     * @param galleryId galleryId
     * @return string
     */
    @GetMapping(value = "/gallery")
    public String gallery(Model model,@PathParam("galleryId") Long galleryId){
        Optional<Gallery> gallery = galleryService.findByGalleryId(galleryId);
        model.addAttribute("gallery",gallery.get());

        model.addAttribute("options",HaloConst.OPTIONS);
        return "admin/widget/_gallery-detail";
    }

    /**
     * 删除图库中的图片
     *
     * @param galleryId galleryId
     * @return string
     */
    @GetMapping(value = "/gallery/remove")
    @ResponseBody
    public boolean removeGallery(@RequestParam("galleryId") Long galleryId){
        try {
            galleryService.removeByGalleryId(galleryId);
        }catch (Exception e){
            log.error("删除图片失败：{0}",e.getMessage());
            return false;
        }
        return true;
    }
}
