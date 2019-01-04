package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.domain.Category;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.domain.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * <pre>
 *     文章持久层
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/11/14
 */
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 查询前五条文章
     *
     * @return List
     */
    @Query(value = "SELECT * FROM halo_post WHERE post_type='post' ORDER BY post_date DESC LIMIT 5", nativeQuery = true)
    List<Post> findTopFive();

    /**
     * 查询所有文章 根据文章类型
     *
     * @param postType post or page
     * @return List
     */
    List<Post> findPostsByPostType(String postType);

    /**
     * 分页查询文章
     *
     * @param postType post or page
     * @param pageable 分页信息
     * @return Page
     */
    Page<Post> findPostsByPostType(String postType, Pageable pageable);

    /**
     * 模糊查询
     *
     * @param keyWord  keyword
     * @param pageable pageable
     * @return List
     */
    List<Post> findByPostTitleLike(String keyWord, Pageable pageable);

    /**
     * 根据文章的状态查询 分页
     *
     * @param status   0，1，2
     * @param postType post or page
     * @param pageable 分页信息
     * @return Page
     */
    Page<Post> findPostsByPostStatusAndPostType(Integer status, String postType, Pageable pageable);

    /**
     * 根据文章的状态查询
     *
     * @param status   0,1,2
     * @param postType post or page
     * @return List
     */
    List<Post> findPostsByPostStatusAndPostType(Integer status, String postType);

    /**
     * 根据路径查询文章
     *
     * @param postUrl  路径
     * @param postType post or page
     * @return Post
     */
    Post findPostByPostUrlAndPostType(String postUrl, String postType);

    /**
     * 根据文章编号查询
     *
     * @param postId   文章编号
     * @param postType post or page
     * @return Post
     */
    Post findPostByPostIdAndPostType(Long postId, String postType);

    /**
     * 查询之后文章
     *
     * @param postDate   发布时间
     * @param postStatus 0，1，2
     * @param postType   post or page
     * @return List
     */
    List<Post> findByPostDateAfterAndPostStatusAndPostTypeOrderByPostDateDesc(Date postDate, Integer postStatus, String postType);


    /**
     * 查询之前的文章
     *
     * @param postDate   发布时间
     * @param postStatus 0，1，2
     * @param postType   post or page
     * @return List
     */
    List<Post> findByPostDateBeforeAndPostStatusAndPostTypeOrderByPostDateAsc(Date postDate, Integer postStatus, String postType);

    /**
     * 查询文章归档信息 根据年份和月份
     *
     * @return List
     */
    @Query(value = "SELECT YEAR(post_date) AS YEAR,MONTH(post_date) AS MONTH,COUNT(*) AS COUNT FROM halo_post WHERE post_status=0 and post_type='post' GROUP BY YEAR(post_date),MONTH(post_date) ORDER BY YEAR DESC,MONTH DESC", nativeQuery = true)
    List<Object[]> findPostGroupByYearAndMonth();

    /**
     * 查询文章归档信息 根据年份
     *
     * @return List
     */
    @Query(value = "SELECT YEAR(post_date) AS YEAR,COUNT(*) AS COUNT FROM halo_post WHERE post_status=0 AND post_type='post' GROUP BY YEAR(post_date) ORDER BY YEAR DESC", nativeQuery = true)
    List<Object[]> findPostGroupByYear();


    /**
     * @Author Aquan
     * @Description 查询文章归档信息 查询所有文章
     * @Date 2019.1.4 11:19
     * @Param
     * @return List
     **/
    @Query(value = "SELECT *,YEAR(post_date) AS YEAR FROM halo_post WHERE post_status=0 AND post_type='post' ORDER BY post_date DESC", nativeQuery = true)
    List<Post> findAllPost();

    /**
     * @Author Aquan
     * @Description 查询文章总数
     * @Date 2019.1.4 15:03
     * @Param
     * @return Integer
     **/
    @Query(value = "SELECT count(1) AS COUNT FROM halo_post WHERE post_status=0 AND post_type='post'", nativeQuery = true)
    Integer totalAllPostCount();



    /**
     * 根据年份和月份查询文章
     *
     * @param year  year
     * @param month month
     * @return List
     */
    @Query(value = "SELECT *,YEAR(post_date) AS YEAR,MONTH(post_date) AS MONTH FROM halo_post WHERE post_status=0 and post_type='post' AND YEAR(post_date)=:year AND MONTH(post_date)=:month ORDER BY post_date DESC", nativeQuery = true)
    List<Post> findPostByYearAndMonth(@Param("year") String year, @Param("month") String month);

    /**
     * 根据年份查询文章
     *
     * @param year year
     * @return List
     */
    @Query(value = "SELECT *,YEAR(post_date) AS YEAR FROM halo_post WHERE post_status=0 AND post_type='post' AND YEAR(post_date)=:year ORDER BY post_date DESC", nativeQuery = true)
    List<Post> findPostByYear(@Param("year") String year);

    /**
     * 根据年份和月份查询文章 分页
     *
     * @param year     year
     * @param month    month
     * @param pageable pageable
     * @return Page
     */
    @Query(value = "SELECT * FROM halo_post WHERE post_status=0 and post_type='post' AND YEAR(post_date)=:year AND MONTH(post_date)=:month ORDER BY post_date DESC", countQuery = "SELECT COUNT(*) FROM halo_post WHERE post_status=0 AND YEAR(post_date)=:year AND MONTH(post_date)=:month", nativeQuery = true)
    Page<Post> findPostByYearAndMonth(@Param("year") String year, @Param("month") String month, Pageable pageable);

    /**
     * 根据分类目录查询文章
     *
     * @param category category
     * @param status   status
     * @param pageable pageable
     * @return Page
     */
    Page<Post> findPostByCategoriesAndPostStatus(Category category, Integer status, Pageable pageable);

    /**
     * 根据标签查询文章，分页
     *
     * @param tag      tag
     * @param status   status
     * @param pageable pageable
     * @return Page
     */
    Page<Post> findPostsByTagsAndPostStatus(Tag tag, Integer status, Pageable pageable);

    /**
     * 根据标签查询文章
     *
     * @param tag tag
     * @return List
     */
    List<Post> findPostsByTags(Tag tag);

    /**
     * 模糊查询文章
     *
     * @param keyword  关键词
     * @param pageable 分页信息
     * @return Page
     */
    @Query(value = "SELECT * FROM halo_post WHERE post_status = 0 AND post_type='post' AND post_title LIKE '%=:keyword%' OR post_content LIKE '%=:keyword%'", nativeQuery = true)
    Page<Post> findPostByPostTitleLikeOrPostContentLikeAndPostTypeAndPostStatus(String keyword, Pageable pageable);

    /**
     * 按热度从大到小排序
     *
     * @param postStatus 文章状态
     * @return List<Post>
     */
    List<Post> findPostsByPostTypeOrderByPostViewsDesc(String postStatus);

    /**
     * 获取所有文章阅读量总和
     *
     * @return Long
     */
    @Query(value = "SELECT SUM(POST_VIEWS) FROM HALO_POST", nativeQuery = true)
    Long getPostViewsSum();

    /**
     * 根据文章状态查询数量
     *
     * @param status   文章状态
     * @param postType 文章类型
     * @return 文章数量
     */
    Integer countAllByPostStatusAndPostType(Integer status, String postType);

    /**
     * 获取指定条数的文章
     *
     * @param limit 条数
     * @return List
     */
    @Query(value = "SELECT * FROM halo_post WHERE post_status = 0 AND post_type = 'post' ORDER BY post_date DESC LIMIT :limit",nativeQuery = true)
    List<Post> getPostsByLimit(@Param(value = "limit") int limit);
}
