package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.model.domain.Category;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.domain.User;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.enums.BlogPropertiesEnum;
import cc.ryanc.halo.service.CategoryService;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.service.UserService;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.crypto.SecureUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * @author : RYAN0UP
 * @date : 2018/12/11
 */
@Slf4j
@RestController
@RequestMapping(value = "/apis/metaweblog", produces = "text/xml;charset=UTF-8")
public class ApiMetaWeBlog {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private PostService postService;


    /**
     * @param request request
     * @return String
     */
    @PostMapping
    public String metaWeBlog(HttpServletRequest request) {
        String responseContent = "";
        String xml;
        try {
            final ServletInputStream inputStream = request.getInputStream();
            xml = IoUtil.read(inputStream, "utf-8");
            final JSONObject requestJSONObject = XML.toJSONObject(xml);

            final JSONObject methodCall = requestJSONObject.getJSONObject("methodCall");
            final String methodName = methodCall.getString("methodName");

            log.info("MetaWeblog[methodName={}]", methodName);

            final JSONArray params = methodCall.getJSONObject("params").getJSONArray("param");

            final String userEmail = params.getJSONObject(1).getJSONObject("value").optString("string");

            final String userPwd = params.getJSONObject(2).getJSONObject("value").get("string").toString();

            final User user = userService.userLoginByEmail(userEmail, SecureUtil.md5(userPwd));

            if (null == user) {
                throw new Exception("用户密码错误！");
            }

            if ("blogger.getUsersBlogs".equals(methodName)) {
                log.info("获取用户博客");
                responseContent = getUserBlogs();
            } else if ("metaWeblog.getCategories".equals(methodName)) {
                log.info("获取分类");
                responseContent = getCategories();
            } else if ("metaWeblog.getRecentPosts".equals(methodName)) {

            } else if ("metaWeblog.newPost".equals(methodName)) {
                Post post = parsetPost(methodCall);
                post.setUser(user);
                post = postService.save(post);
                final StrBuilder strBuilder = new StrBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><methodResponse>");
                strBuilder.append("<params><param><value><string>");
                strBuilder.append(post.getPostId());
                strBuilder.append("</string></value></param></params></methodResponse>");
                responseContent = strBuilder.toString();
            } else if ("metaWeblog.getPost".equals(methodName)) {
                final Long postId = Long.parseLong(params.getJSONObject(0).getJSONObject("value").optString("string"));
                responseContent = getPost(postId);
            } else if ("metaWeblog.editPost".equals(methodName)) {
                Post post = parsetPost(methodCall);
                final Long postId = Long.parseLong(params.getJSONObject(0).getJSONObject("value").optString("string"));
                post.setPostId(postId);
                post.setUser(user);
                postService.save(post);
                final StrBuilder strBuilder = new StrBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><methodResponse>");
                strBuilder.append("<params><param><value><string>");
                strBuilder.append(postId);
                strBuilder.append("</string></value></param></params></methodResponse>");
                responseContent = strBuilder.toString();
            } else if ("blogger.deletePost".equals(methodName)) {
                final Long postId = Long.parseLong(params.getJSONObject(0).getJSONObject("value").optString("string"));
                postService.remove(postId);
                final StrBuilder strBuilder = new StrBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><methodResponse>");
                strBuilder.append("<params><param><value><boolean>");
                strBuilder.append(true);
                strBuilder.append("</boolean></value></param></params></methodResponse>");
                responseContent = strBuilder.toString();
            }

        } catch (final Exception e) {
            e.printStackTrace();
        }
        return responseContent;
    }

    /**
     * @param methodCall
     * @return
     * @throws Exception
     */
    private Post parsetPost(final JSONObject methodCall) throws Exception {
        final Post ret = new Post();

        final JSONArray params = methodCall.getJSONObject("params").getJSONArray("param");
        final JSONObject post = params.getJSONObject(3).getJSONObject("value").getJSONObject("struct");
        final JSONArray members = post.getJSONArray("member");
        for (int i = 0; i < members.length(); i++) {
            final JSONObject member = members.getJSONObject(i);
            final String name = member.getString("name");
            if("dateCreated".equals(name)){
                final String dateString = member.getJSONObject("value").getString("dateTime.iso8601");
                Date date = DateUtil.parseDate(dateString);
                ret.setPostDate(date);
            }else if ("title".equals(name)){
                ret.setPostTitle(member.getJSONObject("value").getString("string"));
            }else if("description".equals(name)){
                final String content = member.getJSONObject("value").optString("string");
                ret.setPostContent(content);
                ret.setPostContentMd(content);
            }else if("categories".equals(name)){
                final StrBuilder cateBuilder = new StrBuilder();
                final JSONObject data = member.getJSONObject("value").getJSONObject("array").getJSONObject("data");
                if(0==data.length()){
                    throw new Exception("At least one category");
                }
            }
        }
        return ret;
    }

    /**
     * 根据文章编号获取文章信息
     *
     * @param postId 文章编号
     * @return 文章信息xml格式
     */
    private String getPost(Long postId) {
        final StrBuilder strBuilder = new StrBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><methodResponse><params><param><value>");
        final String posts = buildPost(postId);
        strBuilder.append(posts);
        strBuilder.append("</value></param></params></methodResponse>");
        return strBuilder.toString();
    }

    /**
     * 根据文章编号构建文章信息
     *
     * @param postId 文章编号
     * @return 文章信息xml格式
     */
    private String buildPost(final Long postId) {
        final StrBuilder strBuilder = new StrBuilder();
        final Post post = postService.findByPostId(postId).orElse(new Post());
        strBuilder.append("<struct>");
        strBuilder.append("<member><name>dateCreated</name>");
        strBuilder.append("<value><dateTime.iso8601>");
        strBuilder.append(DateUtil.format(post.getPostDate(), "yyyy-MM-dd'T'HH:mm:ssZZ"));
        strBuilder.append("</dateTime.iso8601></value></member>");
        strBuilder.append("<member><name>description</name>");
        strBuilder.append("<value>");
        strBuilder.append(post.getPostSummary());
        strBuilder.append("</value></member>");
        strBuilder.append("<member><name>title</name>");
        strBuilder.append("<value>");
        strBuilder.append(post.getPostTitle());
        strBuilder.append("</value></member>");
        strBuilder.append("<member><name>categories</name>");
        strBuilder.append("<value><array><data>");
        List<Category> categories = post.getCategories();
        for (Category category : categories) {
            strBuilder.append("<value>").append(category.getCateName()).append("</value>");
        }
        strBuilder.append("</data></array></value></member></struct>");
        return strBuilder.toString();
    }

    /**
     * 获取用户博客信息
     *
     * @return 用户博客信息xml格式
     */
    private String getUserBlogs() {
        final StrBuilder strBuilder = new StrBuilder(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><methodResponse><params><param><value><array><data><value><struct>");
        final String blogInfo = buildBlogInfo();
        strBuilder.append(blogInfo);
        strBuilder.append("</struct></value></data></array></value></param></params></methodResponse>");

        return strBuilder.toString();
    }

    /**
     * 构建博客信息
     *
     * @return 博客信息xml节点
     */
    private String buildBlogInfo() {
        final String blogId = HaloConst.OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp());
        final String blogTitle = HaloConst.OPTIONS.get(BlogPropertiesEnum.BLOG_TITLE.getProp());
        final StrBuilder strBuilder = new StrBuilder("<member><name>blogid</name><value>");
        strBuilder.append(blogId);
        strBuilder.append("</value></member>");
        strBuilder.append("<member><name>url</name><value>");
        strBuilder.append(HaloConst.OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp()));
        strBuilder.append("</value></member>");
        strBuilder.append("<member><name>blogName</name><value>");
        strBuilder.append(blogTitle);
        strBuilder.append("</value></member>");
        return strBuilder.toString();
    }

    /**
     * 组装分类信息
     *
     * @return 分类信息xml格式
     * @throws Exception Exception
     */
    private String getCategories() throws Exception {
        final StrBuilder strBuilder = new StrBuilder(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><methodResponse><params><param><value><array><data>");
        final String categories = buildCategories();
        strBuilder.append(categories);
        strBuilder.append("</data></array></value></param></params></methodResponse>");

        return strBuilder.toString();
    }

    /**
     * 构建分类信息
     *
     * @return 分类信息xml节点
     * @throws Exception Exception
     */
    private String buildCategories() throws Exception {
        final StrBuilder strBuilder = new StrBuilder();
        final List<Category> categories = categoryService.findAll();
        for (Category category : categories) {
            final String cateName = category.getCateName();
            final Long cateId = category.getCateId();

            strBuilder.append("<value><struct>");
            strBuilder.append("<member><name>description</name>").append("<value>").append(cateName).append("</value></member>");
            strBuilder.append("<member><name>title</name>").append("<value>").append(cateName).append("</value></member>");
            strBuilder.append("<member><name>categoryid</name>").append("<value>").append(cateId).append("</value></member>");
            strBuilder.append("<member><name>htmlUrl</name>").append("<value>").append(HaloConst.OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp())).append("/categories/").append(cateName).append("</value></member>");
            strBuilder.append("</struct></value>");
        }
        return strBuilder.toString();
    }
}
