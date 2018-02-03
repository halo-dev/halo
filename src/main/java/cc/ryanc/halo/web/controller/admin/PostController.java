package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.Category;
import cc.ryanc.halo.model.domain.Logs;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.domain.User;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.LogsRecord;
import cc.ryanc.halo.model.dto.RespStatus;
import cc.ryanc.halo.service.CategoryService;
import cc.ryanc.halo.service.LogsService;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.util.HaloUtil;
import cc.ryanc.halo.web.controller.BaseController;
import lombok.extern.slf4j.Slf4j;
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
    private LogsService logsService;

    @Autowired
    private HttpServletRequest request;

    /**
     * 处理后台获取文章列表的请求
     *
     * @param model Model
     * @param page Page
     * @param size Size
     * @return String
     */
    @GetMapping
    public String posts(Model model,
                        @RequestParam(value = "status",defaultValue = "0") Integer status,
                        @RequestParam(value = "page",defaultValue = "0") Integer page,
                        @RequestParam(value = "size",defaultValue = "10") Integer size){
        Sort sort = new Sort(Sort.Direction.DESC,"postId");
        Pageable pageable = new PageRequest(page,size,sort);
        Page<Post> posts = postService.findPostByStatus(status,pageable);
        model.addAttribute("posts",posts);
        model.addAttribute("publishCount",postService.findPostByStatus(0,pageable).getTotalElements());
        model.addAttribute("draftCount",postService.findPostByStatus(1,pageable).getTotalElements());
        model.addAttribute("trashCount",postService.findPostByStatus(2,pageable).getTotalElements());
        model.addAttribute("options", HaloConst.OPTIONS);
        model.addAttribute("status",status);

        return "admin/admin_post";
    }

    /**
     * 模糊查询文章
     *
     * @param model Model
     * @param keyword keyword
     * @param page page
     * @param size size
     * @return freemarker
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
            model.addAttribute("options", HaloConst.OPTIONS);
        }catch (Exception e){
            log.error("未知错误："+e.getMessage());
        }
        return "admin/admin_post";
    }

    /**
     * 处理预览文章的请求
     *
     * @param postId postId
     * @param model model
     * @return freemarker
     */
    @GetMapping(value = "/view")
    public String viewPost(@PathParam("postId") Long postId,Model model){
        Post post = postService.findByPostId(postId);
        model.addAttribute("post",post);
        model.addAttribute("options", HaloConst.OPTIONS);
        return this.render("post");
    }

    /**
     * 处理跳转到新建文章页面
     *
     * @return freemarker
     */
    @GetMapping(value = "/new")
    public String newPost(Model model){
        try {
            List<Category> categories = categoryService.findAllCategories();
            model.addAttribute("categories",categories);
            model.addAttribute("options", HaloConst.OPTIONS);
            model.addAttribute("btnPush","发布");
        }catch (Exception e){
            log.error("未知错误："+e.getMessage());
        }
        return "admin/admin_editor";
    }

    /**
     * 添加文章
     *
     * @param post Post
     */
    @PostMapping(value = "/new/push")
    @ResponseBody
    public void pushPost(@ModelAttribute Post post,@RequestParam("cateList") List<String> cateList, HttpSession session){
        try{
            for(String a:cateList){
                System.out.println(a);
            }
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
            User user = (User)session.getAttribute("user");
            post.setUser(user);
            List<Category> categories = categoryService.strListToCateList(cateList);
            post.setCategories(categories);
            postService.saveByPost(post);
            log.info("已发表新文章："+post.getPostTitle());
            logsService.saveByLogs(new Logs(LogsRecord.PUSH_POST,post.getPostTitle(),HaloUtil.getIpAddr(request),HaloUtil.getDate()));
        }catch (Exception e){
            log.error("未知错误："+e.getMessage());
        }
    }

    /**
     * 处理移至回收站的请求
     *
     * @param postId postId
     * @return String
     */
    @GetMapping("/throw")
    public String moveToTrash(@RequestParam("postId") Long postId){
        try{
            postService.updatePostStatus(postId,2);
            log.info("编号为"+postId+"的文章已被移到回收站");
        }catch (Exception e){
            log.error("未知错误："+e.getMessage());
        }
        return "redirect:/admin/posts";
    }

    /**
     * 处理文章为发布的状态
     *
     * @param postId postId
     * @return String
     */
    @GetMapping("/revert")
    public String moveToPublish(@RequestParam("postId") Long postId,
                                @RequestParam("status") Integer status){
        try{
            postService.updatePostStatus(postId,0);
            log.info("编号为"+postId+"的文章已改变为发布状态");
        }catch (Exception e){
            log.error("未知错误："+e.getMessage());
        }
        return "redirect:/admin/posts?status="+status;
    }

    /**
     * 处理删除文章的请求
     *
     * @param postId postId
     * @return 转发
     */
    @GetMapping(value = "/remove")
    public String removePost(@PathParam("postId") Long postId){
        try{
            Post post = postService.findByPostId(postId);
            postService.removeByPostId(postId);
            log.info("删除的文章为："+post.getPostTitle());
            logsService.saveByLogs(new Logs(LogsRecord.REMOVE_POST,post.getPostTitle(),HaloUtil.getIpAddr(request),HaloUtil.getDate()));
        }catch (Exception e){
            log.error("未知错误："+e.getMessage());
        }
        return "redirect:/admin/posts?status=2";
    }

    /**
     * 跳转到编辑文章页面
     *
     * @param postId postId
     * @param model Model
     * @return String
     */
    @GetMapping(value = "/edit")
    public String editPost(@PathParam("postId") Long postId, Model model){
        try {
            Post post = postService.findByPostId(postId);
            model.addAttribute("post",post);
            List<Category> categories = categoryService.findAllCategories();
            model.addAttribute("categories",categories);
            model.addAttribute("btnPush","更新");
            model.addAttribute("options", HaloConst.OPTIONS);
        }catch (Exception e){
            log.error("未知错误："+e.getMessage());
        }
        return "admin/admin_editor";
    }

    /**
     * 更新所有摘要
     *
     * @param postSummary postSummary
     * @return string
     */
    @GetMapping(value = "/updateSummary")
    @ResponseBody
    public String updateSummary(@PathParam("postSummary") Integer postSummary){
        try {
            postService.updateAllSummary(postSummary);
            return RespStatus.SUCCESS;
        }catch (Exception e){
            log.error("未知错误："+e.getMessage());
            return RespStatus.ERROR;
        }
    }

    /**
     * 验证文章路径是否已经存在
     *
     * @param postUrl postUrl
     * @return String
     */
    @GetMapping(value = "/checkUrl")
    @ResponseBody
    public String checkUrlExists(@PathParam("postUrl") String postUrl){
        Post post = postService.findByPostUrl(postUrl);
        if(null!=post){
            return RespStatus.EXISTS;
        }else{
            return RespStatus.NOTEXISTS;
        }
    }
}
