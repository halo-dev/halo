package cc.ryanc.halo.service;

import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.dto.Archive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author : RYAN0UP
 * @date : 2017/11/14
 * @version : 1.0
 * description :
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
     * 修改文章
     *
     * @param post Post
     * @return Post
     */
    Post updateByPost(Post post);

    /**
     * 修改文章状态
     *
     * @param postId postId
     * @param status status
     * @return Post
     */
    Post updatePostStatus(Long postId,Integer status);

    /**
     * 批量修改摘要
     *
     * @param postSummary postSummary
     */
    void updateAllSummary(Integer postSummary);

    /**
     * 获取文章列表 分页
     *
     * @param pageable Pageable
     * @return Page
     */
    Page<Post> findAllPosts(Pageable pageable);

    /**
     * 获取文章列表 不分页
     *
     * @return List
     */
    List<Post> findAllPosts();

    /**
     * 模糊查询文章
     *
     * @param keyWord keyword
     * @param pageable pageable
     * @return list
     */
    List<Post> searchPosts(String keyWord,Pageable pageable);

    /**
     * 根据文章状态查询 分页
     *
     * @param status status
     * @param pageable pageable
     * @return page
     */
    Page<Post> findPostByStatus(Integer status,Pageable pageable);

    /**
     * 根据文章状态查询
     *
     * @param status status
     * @return list
     */
    List<Post> findPostByStatus(Integer status);

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
     * @param postUrl postUrl
     * @return post
     */
    Post findByPostUrl(String postUrl);

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
     * @param year year
     * @param month month
     * @return list
     */
    List<Post> findPostByYearAndMonth(String year,String month);

    /**
     * 根据年份和月份查询文章 分页
     *
     * @param year year
     * @param month month
     * @param pageable pageable
     * @return page
     */
    Page<Post> findPostByYearAndMonth(String year,String month, Pageable pageable);

    /**
     * 根据年份查询文章
     *
     * @param year year
     * @return list
     */
    List<Post> findPostByYear(String year);

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
