package cc.ryanc.halo.service;

import cc.ryanc.halo.model.domain.Category;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.domain.Tag;
import cc.ryanc.halo.model.dto.Archive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
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
    Post saveByPost(Post post);

    /**
     * 根据编号删除文章
     *
     * @param postId postId
     * @return Post
     */
    Post removeByPostId(Long postId);

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
     * 获取文章列表 分页
     *
     * @param postType post or page
     * @param pageable 分页信息
     * @return Page<Post></>
     */
    Page<Post> findAllPosts(String postType, Pageable pageable);

    /**
     * 获取文章列表 不分页
     *
     * @param postType post or page
     * @return List<Post></>
     */
    List<Post> findAllPosts(String postType);

    /**
     * 模糊查询文章
     *
     * @param keyWord  keyword
     * @param pageable pageable
     * @return list
     */
    List<Post> searchPosts(String keyWord, Pageable pageable);

    /**
     * 根据文章状态查询 分页
     *
     * @param status   0，1，2
     * @param postType post or page
     * @param pageable 分页信息
     * @return Page<Post></>
     */
    Page<Post> findPostByStatus(Integer status, String postType, Pageable pageable);

    /**
     * 根据文章状态查询
     *
     * @param status   0，1，2
     * @param postType post or page
     * @return List<Post></>
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
     * 查询Id之后的文章
     *
     * @param postDate postDate
     * @return post
     */
    List<Post> findByPostDateAfter(Date postDate);

    /**
     * 查询Id之前的文章
     *
     * @param postDate postDate
     * @return list
     */
    List<Post> findByPostDateBefore(Date postDate);

    /**
     * 查询归档信息 根据年份和月份
     *
     * @return List
     */
    List<Archive> findPostGroupByYearAndMonth();

    /**
     * 查询归档信息 根据年份
     *
     * @return list
     */
    List<Archive> findPostGroupByYear();

    /**
     * 根据年份和月份查询文章
     *
     * @param year  year
     * @param month month
     * @return list
     */
    List<Post> findPostByYearAndMonth(String year, String month);

    /**
     * 根据年份和月份查询文章 分页
     *
     * @param year     year
     * @param month    month
     * @param pageable pageable
     * @return page
     */
    Page<Post> findPostByYearAndMonth(String year, String month, Pageable pageable);

    /**
     * 根据年份查询文章
     *
     * @param year year
     * @return list
     */
    List<Post> findPostByYear(String year);

    /**
     * 根据分类目录查询文章
     *
     * @param category category
     * @param pageable pageable
     * @return Page<Post></>
     */
    Page<Post> findPostByCategories(Category category,Pageable pageable);

    /**
     * 根据标签查询文章
     *
     * @param tag      tag
     * @param pageable pageable
     * @return page
     */
    Page<Post> findPostsByTags(Tag tag, Pageable pageable);

    /**
     * 搜索文章
     *
     * @param keyword 关键词
     * @param pageable 分页信息
     * @return Page<Post></>
     */
    Page<Post> searchByKeywords(String keyword,Pageable pageable);

    /**
     * 热门文章
     *
     * @return List<Post>
     */
    List<Post> hotPosts();

    /**
     * 生成rss
     *
     * @param posts posts
     * @return string
     */
    String buildRss(List<Post> posts);

    /**
     * 生成sitemap
     *
     * @param posts posts
     * @return string
     */
    String buildSiteMap(List<Post> posts);
}
