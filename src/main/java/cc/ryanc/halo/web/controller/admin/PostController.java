package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.*;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.LogsRecord;
import cc.ryanc.halo.service.*;
import cc.ryanc.halo.util.HaloUtil;
import cc.ryanc.halo.web.controller.core.BaseController;
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
import java.util.Map;
import java.util.Optional;

/**
 * @author : RYAN0UP
 * @date : 2017/12/10
 * @version : 1.0
 * description: 文章控制器
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/posts")
public class PostController extends BaseController{

    @Autowired
    private PostService postService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TagService tagService;

    @Autowired
    private LogsService logsService;

    @Autowired
    private OptionsService optionsService;

    @Autowired
    private HttpServletRequest request;

    /**
     * 处理后台获取文章列表的请求
     *
     * @param model model
     * @param page 当前页码
     * @param size 每页显示的条数
     * @return 模板路径admin/admin_post
     */
    @GetMapping
    public String posts(Model model,
                        @RequestParam(value = "status",defaultValue = "0") Integer status,
                        @RequestParam(value = "page",defaultValue = "0") Integer page,
                        @RequestParam(value = "size",defaultValue = "10") Integer size){
        Sort sort = new Sort(Sort.Direction.DESC,"postDate");
        Pageable pageable = new PageRequest(page,size,sort);
        Page<Post> posts = postService.findPostByStatus(status,HaloConst.POST_TYPE_POST,pageable);
        model.addAttribute("posts",posts);
        model.addAttribute("publishCount",postService.findPostByStatus(0,HaloConst.POST_TYPE_POST,pageable).getTotalElements());
        model.addAttribute("draftCount",postService.findPostByStatus(1,HaloConst.POST_TYPE_POST,pageable).getTotalElements());
        model.addAttribute("trashCount",postService.findPostByStatus(2,HaloConst.POST_TYPE_POST,pageable).getTotalElements());
        model.addAttribute("status",status);
        return "admin/admin_post";
    }

    /**
     * 模糊查询文章
     *
     * @param model Model
     * @param keyword keyword 关键字
     * @param page page 当前页码
     * @param size size 每页显示条数
     * @return 模板路径admin/admin_post
     */
    @PostMapping(value="/search")
    public String searchPost(Model model,
                             @RequestParam(value = "keyword") String keyword,
                             @RequestParam(value = "page",defaultValue = "0") Integer page,
                             @RequestParam(value = "size",defaultValue = "10") Integer size){
        try {
            //排序规则
            Sort sort = new Sort(Sort.Direction.DESC,"postId");
            Pageable pageable = new PageRequest(page,size,sort);
            model.addAttribute("posts",postService.searchPosts(keyword,pageable));
        }catch (Exception e){
            log.error("未知错误：{0}",e.getMessage());
        }
        return "admin/admin_post";
    }

    /**
     * 处理预览文章的请求
     *
     * @param postId 文章编号
     * @param model model
     * @return 模板路径/themes/{theme}/post
     */
    @GetMapping(value = "/view")
    public String viewPost(@PathParam("postId") Long postId,Model model){
        Optional<Post> post = postService.findByPostId(postId);
        model.addAttribute("post",post.get());
        return this.render("post");
    }

    /**
     * 处理跳转到新建文章页面
     *
     * @param model model
     * @return 模板路径admin/admin_editor
     */
    @GetMapping(value = "/new")
    public String newPost(Model model){
        List<Category> categories = categoryService.findAllCategories();
        List<Tag> tags = tagService.findAllTags();
        model.addAttribute("categories",categories);
        model.addAttribute("tags",tags);
        return "admin/admin_post_md_editor";
    }

    /**
     * 添加文章
     *
     * @param post Post实体
     * @param cateList 分类列表
     * @param tagList 标签列表
     * @param session session
     */
    @PostMapping(value = "/new/push")
    @ResponseBody
    public void pushPost(@ModelAttribute Post post, @RequestParam("cateList") List<String> cateList, @RequestParam("tagList") String tagList, HttpSession session){
        try{
            //提取摘要
            int postSummary = 50;
            if(HaloUtil.isNotNull(HaloConst.OPTIONS.get("post_summary"))){
                postSummary = Integer.parseInt(HaloConst.OPTIONS.get("post_summary"));
            }
            if(HaloUtil.htmlToText(post.getPostContent()).length()>postSummary){
                String summary = HaloUtil.getSummary(post.getPostContent(), postSummary);
                post.setPostSummary(summary);
            }
            post.setPostDate(HaloUtil.getDate());
            //发表用户
            User user = (User)session.getAttribute(HaloConst.USER_SESSION_KEY);
            post.setUser(user);
            List<Category> categories = categoryService.strListToCateList(cateList);
            post.setCategories(categories);
            if(StringUtils.isNotEmpty(tagList)){
                List<Tag> tags = tagService.strListToTagList(StringUtils.trim(tagList));
                post.setTags(tags);
            }
            postService.saveByPost(post);
            logsService.saveByLogs(new Logs(LogsRecord.PUSH_POST,post.getPostTitle(),HaloUtil.getIpAddr(request),HaloUtil.getDate()));
        }catch (Exception e){
            log.error("未知错误：{0}",e.getMessage());
        }
    }

    /**
     * 处理移至回收站的请求
     *
     * @param postId 文章编号
     * @return 重定向到/admin/posts
     */
    @GetMapping("/throw")
    public String moveToTrash(@RequestParam("postId") Long postId){
        try{
            postService.updatePostStatus(postId,2);
            log.info("编号为"+postId+"的文章已被移到回收站");
        }catch (Exception e){
            log.error("未知错误：{0}",e.getMessage());
        }
        return "redirect:/admin/posts";
    }

    /**
     * 处理文章为发布的状态
     *
     * @param postId 文章编号
     * @return 重定向到/admin/posts
     */
    @GetMapping("/revert")
    public String moveToPublish(@RequestParam("postId") Long postId,
                                @RequestParam("status") Integer status){
        try{
            postService.updatePostStatus(postId,0);
            log.info("编号为"+postId+"的文章已改变为发布状态");
        }catch (Exception e){
            log.error("未知错误：{0}",e.getMessage());
        }
        return "redirect:/admin/posts?status="+status;
    }

    /**
     * 处理删除文章的请求
     *
     * @param postId 文章编号
     * @return 重定向到/admin/posts
     */
    @GetMapping(value = "/remove")
    public String removePost(@PathParam("postId") Long postId,@PathParam("postType") String postType){
        try{
            Optional<Post> post = postService.findByPostId(postId);
            postService.removeByPostId(postId);
            logsService.saveByLogs(new Logs(LogsRecord.REMOVE_POST,post.get().getPostTitle(),HaloUtil.getIpAddr(request),HaloUtil.getDate()));
        }catch (Exception e){
            log.error("未知错误：{0}",e.getMessage());
        }
        if(StringUtils.equals(HaloConst.POST_TYPE_POST,postType)){
            return "redirect:/admin/posts?status=2";
        }
        return "redirect:/admin/page";
    }

    /**
     * 跳转到编辑文章页面
     *
     * @param postId 文章编号
     * @param model model
     * @return 模板路径admin/admin_editor
     */
    @GetMapping(value = "/edit")
    public String editPost(@PathParam("postId") Long postId, Model model){
        Optional<Post> post = postService.findByPostId(postId);
        model.addAttribute("post",post.get());
        List<Category> categories = categoryService.findAllCategories();
        model.addAttribute("categories",categories);
        return "admin/admin_post_md_editor";
    }

    /**
     * 更新所有摘要
     *
     * @param postSummary 文章摘要字数
     * @return true：更新成功，false：更新失败
     */
    @GetMapping(value = "/updateSummary")
    @ResponseBody
    public boolean updateSummary(@PathParam("postSummary") Integer postSummary){
        try {
            postService.updateAllSummary(postSummary);
            return true;
        }catch (Exception e){
            log.error("未知错误：{0}",e.getMessage());
            return false;
        }
    }

    /**
     * 验证文章路径是否已经存在
     *
     * @param postUrl 文章路径
     * @return true：不存在，false：已存在
     */
    @GetMapping(value = "/checkUrl")
    @ResponseBody
    public boolean checkUrlExists(@PathParam("postUrl") String postUrl){
        Post post = postService.findByPostUrl(postUrl,HaloConst.POST_TYPE_POST);
        return null!=post;
    }

    /**
     * 将所有文章推送到百度
     * @param baiduToken baiduToken
     * @return true or false
     */
    @GetMapping(value = "/pushAllToBaidu")
    @ResponseBody
    public boolean pushAllToBaidu(@PathParam("baiduToken") String baiduToken){
        if(StringUtils.isEmpty(baiduToken)){
            return false;
        }
        String blogUrl = optionsService.findOneOption("blog_url");
        List<Post> posts = postService.findAllPosts(HaloConst.POST_TYPE_POST);
        String urls = "";
        for(Post post:posts){
            urls+=blogUrl+"/archives/"+post.getPostUrl()+"\n";
        }
        String result = HaloUtil.baiduPost(blogUrl,baiduToken,urls);
        log.info(result);
        return true;
    }
}
