package cc.ryanc.halo.service;

import cc.ryanc.halo.model.domain.Category;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.domain.Tag;
import cc.ryanc.halo.model.dto.Archive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * <pre>
 *     文章/页面业务逻辑接口
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/11/14
 */
public interface PostService {

    /**
     * 新增文章
     *
     * @param post Post
     * @return Post
     */
    Post save(Post post);

    /**
     * 根据编号删除文章
     *
     * @param postId postId
     * @return Post
     */
    Post remove(Long postId);

    /**
     * 修改文章状态
     *
     * @param postId postId
     * @param status status
     * @return Post
     */
    Post updatePostStatus(Long postId, Integer status);

    /**
     * 批量修改摘要
     *
     * @param postSummary postSummary
     */
    void updateAllSummary(Integer postSummary);

    /**
     * 获取文章列表 不分页
     *
     * @param postType post or page
     * @return List
     */
    List<Post> findAll(String postType);

    /**
     * 模糊查询文章
     *
     * @param keyword    关键词
     * @param postType   文章类型
     * @param postStatus 文章状态
     * @param pageable   分页信息
     * @return Page
     */
    Page<Post> searchPosts(String keyword, String postType, Integer postStatus, Pageable pageable);

    /**
     * 根据文章状态查询 分页，用于后台管理
     *
     * @param status   0，1，2
     * @param postType post or page
     * @param pageable 分页信息
     * @return Page
     */
    Page<Post> findPostByStatus(Integer status, String postType, Pageable pageable);

    /**
     * 根据文章状态查询 分页，首页分页
     *
     * @param pageable pageable
     * @return Page
     */
    Page<Post> findPostByStatus(Pageable pageable);

    /**
     * 根据文章状态查询
     *
     * @param status   0，1，2
     * @param postType post or page
     * @return List
     */
    List<Post> findPostByStatus(Integer status, String postType);

    /**
     * 根据编号查询文章
     *
     * @param postId postId
     * @return Post
     */
    Optional<Post> findByPostId(Long postId);

    /**
     * 根据编号和类型查询文章
     *
     * @param postId   postId
     * @param postType postType
     * @return Post
     */
    Post findByPostId(Long postId, String postType);

    /**
     * 根据文章路径查询
     *
     * @param postUrl  路径
     * @param postType post or page
     * @return Post
     */
    Post findByPostUrl(String postUrl, String postType);

    /**
     * 查询前五条数据
     *
     * @return List
     */
    List<Post> findPostLatest();

    /**
     * 获取下一篇文章 较新
     *
     * @param postDate postDate
     * @return Post
     */
    Post getNextPost(Date postDate);

    /**
     * 获取下一篇文章 较老
     *
     * @param postDate postDate
     * @return Post
     */
    Post getPrePost(Date postDate);

    /**
     * 查询归档信息 根据年份和月份
     *
     * @return List
     */
    List<Archive> findPostGroupByYearAndMonth();

    /**
     * 查询归档信息 根据年份
     *
     * @return List
     */
    List<Archive> findPostGroupByYear();

    /**
     * @return List
     * @Author Aquan
     * @Description 查询归档信息 查看所有文章
     * @Date 2019.1.4 11:14
     * @Param
     **/
    List<Archive> findAllPost();


    /**
     * 根据年份和月份查询文章
     *
     * @param year  year
     * @param month month
     * @return List
     */
    List<Post> findPostByYearAndMonth(String year, String month);

    /**
     * 根据年份和月份查询文章 分页
     *
     * @param year     year
     * @param month    month
     * @param pageable pageable
     * @return Page
     */
    Page<Post> findPostByYearAndMonth(String year, String month, Pageable pageable);

    /**
     * 根据年份查询文章
     *
     * @param year year
     * @return List
     */
    List<Post> findPostByYear(String year);

    /**
     * 根据分类目录查询文章
     *
     * @param category category
     * @param pageable pageable
     * @return Page
     */
    Page<Post> findPostByCategories(Category category, Pageable pageable);

    /**
     * 根据标签查询文章
     *
     * @param tag      tag
     * @param pageable pageable
     * @return Page
     */
    Page<Post> findPostsByTags(Tag tag, Pageable pageable);

    /**
     * 热门文章
     *
     * @return List
     */
    List<Post> hotPosts();

    /**
     * 当前文章的相似文章
     *
     * @param post post
     * @return List
     */
    List<Post> relatedPosts(Post post);

    /**
     * 获取所有文章的阅读量
     *
     * @return Long
     */
    Long getPostViews();

    /**
     * 根据文章状态查询数量
     *
     * @param status 文章状态
     * @return 文章数量
     */
    Integer getCountByStatus(Integer status);

    /**
     * 生成rss
     *
     * @param posts posts
     * @return String
     */
    String buildRss(List<Post> posts);

    /**
     * 生成sitemap
     *
     * @param posts posts
     * @return String
     */
    String buildSiteMap(List<Post> posts);

    /**
     * 缓存阅读数
     *
     * @param postId postId
     */
    void cacheViews(Long postId);

    /**
     * 组装分类目录和标签
     *
     * @param post     post
     * @param cateList cateList
     * @param tagList  tagList
     * @return Post Post
     */
    Post buildCategoriesAndTags(Post post, List<String> cateList, @RequestParam("tagList") String tagList);

    /**
     * 获取最近的文章
     *
     * @param limit 条数
     * @return List
     */
    List<Post> getRecentPosts(int limit);
}
