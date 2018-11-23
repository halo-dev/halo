package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.*;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.dto.LogsRecord;
import cc.ryanc.halo.model.enums.BlogPropertiesEnum;
import cc.ryanc.halo.model.enums.PostTypeEnum;
import cc.ryanc.halo.model.enums.ResultCodeEnum;
import cc.ryanc.halo.service.GalleryService;
import cc.ryanc.halo.service.LinkService;
import cc.ryanc.halo.service.LogsService;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.utils.HaloUtils;
import cc.ryanc.halo.utils.LocaleMessageUtil;
import cc.ryanc.halo.utils.MarkdownUtils;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * <pre>
 *     后台页面管理控制器
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/12/10
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/page")
public class PageController {

    @Autowired
    LocaleMessageUtil localeMessageUtil;
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
        List<Post> posts = postService.findAllPosts(PostTypeEnum.POST_TYPE_PAGE.getDesc());
        model.addAttribute("pages", posts);
        return "admin/admin_page";
    }

    /**
     * 获取友情链接列表并渲染页面
     *
     * @return 模板路径admin/admin_page_link
     */
    @GetMapping(value = "/links")
    public String links() {
        return "admin/admin_page_link";
    }

    /**
     * 跳转到修改页面
     *
     * @param model  model
     * @param linkId linkId 友情链接编号
     * @return String 模板路径admin/admin_page_link
     */
    @GetMapping(value = "/links/edit")
    public String toEditLink(Model model, @RequestParam("linkId") Long linkId) {
        Optional<Link> link = linkService.findByLinkId(linkId);
        model.addAttribute("updateLink", link.get());
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
            linkService.saveByLink(link);
        } catch (Exception e) {
            log.error("Save/modify friendship link failed: {}", e.getMessage());
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
    public String removeLink(@RequestParam("linkId") Long linkId) {
        try {
            linkService.removeByLinkId(linkId);
        } catch (Exception e) {
            log.error("Deleting a friendship link failed: {}", e.getMessage());
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
        Pageable pageable = PageRequest.of(page, size, sort);
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
            if (StrUtil.isEmpty(gallery.getGalleryThumbnailUrl())) {
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
    public String gallery(Model model, @RequestParam("galleryId") Long galleryId) {
        Optional<Gallery> gallery = galleryService.findByGalleryId(galleryId);
        model.addAttribute("gallery", gallery.get());
        return "admin/widget/_gallery-detail";
    }

    /**
     * 删除图库中的图片
     *
     * @param galleryId 图片编号
     * @return JsonResult
     */
    @GetMapping(value = "/gallery/remove")
    @ResponseBody
    public JsonResult removeGallery(@RequestParam("galleryId") Long galleryId) {
        try {
            galleryService.removeByGalleryId(galleryId);
        } catch (Exception e) {
            log.error("Failed to delete image: {}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-success"));
    }

    /**
     * 跳转到新建页面
     *
     * @param model model
     * @return 模板路径admin/admin_page_md_editor
     */
    @GetMapping(value = "/new")
    public String newPage(Model model) {
        List<String> customTpls = HaloUtils.getCustomTpl(HaloConst.OPTIONS.get(BlogPropertiesEnum.THEME.getProp()));
        model.addAttribute("customTpls",customTpls);
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
    public JsonResult pushPage(@ModelAttribute Post post, HttpSession session) {
        String msg = localeMessageUtil.getMessage("code.admin.common.save-success");
        try {
            //发表用户
            User user = (User) session.getAttribute(HaloConst.USER_SESSION_KEY);
            post.setUser(user);
            post.setPostType(PostTypeEnum.POST_TYPE_PAGE.getDesc());
            if (null != post.getPostId()) {
                Post oldPost = postService.findByPostId(post.getPostId()).get();
                if (null == post.getPostDate()) {
                    post.setPostDate(DateUtil.date());
                }
                post.setPostViews(oldPost.getPostViews());
                msg = localeMessageUtil.getMessage("code.admin.common.update-success");
            } else {
                post.setPostDate(DateUtil.date());
            }
            post.setPostUpdate(DateUtil.date());
            post.setPostContent(MarkdownUtils.renderMarkdown(post.getPostContentMd()));
            //当没有选择文章缩略图的时候，自动分配一张内置的缩略图
            if (StrUtil.equals(post.getPostThumbnail(), BlogPropertiesEnum.DEFAULT_THUMBNAIL.getProp())) {
                post.setPostThumbnail("/static/images/thumbnail/thumbnail-" + RandomUtil.randomInt(1, 10) + ".jpg");
            }
            postService.saveByPost(post);
            logsService.saveByLogs(new Logs(LogsRecord.PUSH_PAGE, post.getPostTitle(), ServletUtil.getClientIP(request), DateUtil.date()));
            return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), msg);
        } catch (Exception e) {
            log.error("Save page failed: {}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.save-failed"));
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
    public String editPage(@RequestParam("pageId") Long pageId, Model model) {
        Optional<Post> post = postService.findByPostId(pageId);
        List<String> customTpls = HaloUtils.getCustomTpl(HaloConst.OPTIONS.get(BlogPropertiesEnum.THEME.getProp()));
        model.addAttribute("post", post.orElse(new Post()));
        model.addAttribute("customTpls",customTpls);
        return "admin/admin_page_md_editor";
    }

    /**
     * 检查该路径是否已经存在
     *
     * @param postUrl postUrl
     * @return JsonResult
     */
    @GetMapping(value = "/checkUrl")
    @ResponseBody
    public JsonResult checkUrlExists(@RequestParam("postUrl") String postUrl) {
        Post post = postService.findByPostUrl(postUrl, PostTypeEnum.POST_TYPE_PAGE.getDesc());
        if (null != post) {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.url-is-exists"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), "");
    }

    @InitBinder
    public void initBinder(ServletRequestDataBinder binder) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(sdf, true));
    }
}
