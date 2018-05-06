package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.*;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.LogsRecord;
import cc.ryanc.halo.service.GalleryService;
import cc.ryanc.halo.service.LinkService;
import cc.ryanc.halo.service.LogsService;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.util.HaloUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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

    @Autowired
    private PostService postService;

    @Autowired
    private LogsService logsService;

    @Autowired
    private HttpServletRequest request;

    /**
     * 页面管理页面
     *
     * @param model model
     * @return 模板路径admin/admin_page
     */
    @GetMapping
    public String pages(Model model) {
        List<Post> posts = postService.findAllPosts(HaloConst.POST_TYPE_PAGE);
        model.addAttribute("pages", posts);
        return "admin/admin_page";
    }

    /**
     * 获取友情链接列表并渲染页面
     *
     * @param model model
     * @return 模板路径admin/admin_page_link
     */
    @GetMapping(value = "/links")
    public String links(Model model) {
        List<Link> links = linkService.findAllLinks();
        model.addAttribute("links", links);
        model.addAttribute("statusName", "添加");
        return "admin/admin_page_link";
    }

    /**
     * 跳转到修改页面
     *
     * @param model  model
     * @param linkId linkId 友情链接编号
     * @return String 模板路径admin/admin_page_link
     */
    @GetMapping("/links/edit")
    public String toEditLink(Model model, @PathParam("linkId") Long linkId) {
        List<Link> links = linkService.findAllLinks();
        Optional<Link> link = linkService.findByLinkId(linkId);
        model.addAttribute("updateLink", link.get());
        model.addAttribute("statusName", "修改");
        model.addAttribute("links", links);
        return "admin/admin_page_link";
    }

    /**
     * 处理添加/修改友链的请求并渲染页面
     *
     * @param link Link实体
     * @return 重定向到/admin/page/links
     */
    @PostMapping(value = "/links/save")
    public String saveLink(@ModelAttribute Link link) {
        try {
            Link backLink = linkService.saveByLink(link);
            log.info("保存成功，数据为：" + backLink);
        } catch (Exception e) {
            log.error("未知错误：{0}", e.getMessage());
        }
        return "redirect:/admin/page/links";
    }

    /**
     * 处理删除友情链接的请求并重定向
     *
     * @param linkId 友情链接编号
     * @return 重定向到/admin/page/links
     */
    @GetMapping(value = "/links/remove")
    public String removeLink(@PathParam("linkId") Long linkId) {
        try {
            Link link = linkService.removeByLinkId(linkId);
            log.info("删除的友情链接：" + link);
        } catch (Exception e) {
            log.error("未知错误：{0}", e.getMessage());
        }
        return "redirect:/admin/page/links";
    }

    /**
     * 图库管理
     *
     * @param model model
     * @param page  当前页码
     * @param size  每页显示的条数
     * @return 模板路径admin/admin_page_gallery
     */
    @GetMapping(value = "/galleries")
    public String gallery(Model model,
                          @RequestParam(value = "page", defaultValue = "0") Integer page,
                          @RequestParam(value = "size", defaultValue = "18") Integer size) {
        Sort sort = new Sort(Sort.Direction.DESC, "galleryId");
        Pageable pageable = new PageRequest(page, size, sort);
        Page<Gallery> galleries = galleryService.findAllGalleries(pageable);
        model.addAttribute("galleries", galleries);
        return "admin/admin_page_gallery";
    }

    /**
     * 保存图片
     *
     * @param gallery gallery
     * @return 重定向到/admin/page/gallery
     */
    @PostMapping(value = "/gallery/save")
    public String saveGallery(@ModelAttribute Gallery gallery) {
        try {
            if (StringUtils.isEmpty(gallery.getGalleryThumbnailUrl())) {
                gallery.setGalleryThumbnailUrl(gallery.getGalleryUrl());
            }
            galleryService.saveByGallery(gallery);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/page/galleries";
    }

    /**
     * 处理获取图片详情的请求
     *
     * @param model     model
     * @param galleryId 图片编号
     * @return 模板路径admin/widget/_gallery-detail
     */
    @GetMapping(value = "/gallery")
    public String gallery(Model model, @PathParam("galleryId") Long galleryId) {
        Optional<Gallery> gallery = galleryService.findByGalleryId(galleryId);
        model.addAttribute("gallery", gallery.get());
        return "admin/widget/_gallery-detail";
    }

    /**
     * 删除图库中的图片
     *
     * @param galleryId 图片编号
     * @return true：删除成功，false：删除失败
     */
    @GetMapping(value = "/gallery/remove")
    @ResponseBody
    public boolean removeGallery(@RequestParam("galleryId") Long galleryId) {
        try {
            galleryService.removeByGalleryId(galleryId);
        } catch (Exception e) {
            log.error("删除图片失败：{0}", e.getMessage());
            return false;
        }
        return true;
    }


    /**
     * 跳转到新建页面
     *
     * @return 模板路径admin/admin_page_md_editor
     */
    @GetMapping(value = "/new")
    public String newPage(Model model) {
        return "admin/admin_page_md_editor";
    }

    /**
     * 发表页面
     *
     * @param post    post
     * @param session session
     */
    @PostMapping(value = "/new/push")
    @ResponseBody
    public void pushPage(@ModelAttribute Post post, HttpSession session) {
        try {
            post.setPostDate(HaloUtil.getDate());
            //发表用户
            User user = (User) session.getAttribute(HaloConst.USER_SESSION_KEY);
            post.setUser(user);
            post.setPostType(HaloConst.POST_TYPE_PAGE);
            postService.saveByPost(post);
            logsService.saveByLogs(new Logs(LogsRecord.PUSH_POST, post.getPostTitle(), HaloUtil.getIpAddr(request), HaloUtil.getDate()));
        } catch (Exception e) {
            log.error("未知错误：{0}", e.getMessage());
        }
    }

    /**
     * 跳转到修改页面
     *
     * @param pageId 页面编号
     * @param model  model
     * @return admin/admin_page_md_editor
     */
    @GetMapping(value = "/edit")
    public String editPage(@PathParam("pageId") Long pageId, Model model) {
        Optional<Post> post = postService.findByPostId(pageId);
        model.addAttribute("post", post.get());
        return "admin/admin_page_md_editor";
    }

    /**
     * 检查该路径是否已经存在
     *
     * @param postUrl postUrl
     * @return true or false
     */
    @GetMapping(value = "/checkUrl")
    @ResponseBody
    public boolean checkUrlExists(@PathParam("postUrl") String postUrl) {
        Post post = postService.findByPostUrl(postUrl, HaloConst.POST_TYPE_PAGE);
        // TODO 还没写完
        if (null != post || StringUtils.equals("archives", postUrl) || StringUtils.equals("galleries", postUrl)) {
            return true;
        }
        return false;
    }
}
