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
 * @author : RYAN0UP
 * @date : 2017/11/14
 * @version : 1.0
 */
public interface PostRepository extends JpaRepository<Post,Long>{

    /**
     * 查询前五条文章
     *
     * @return list
     */
    @Query(value = "SELECT * FROM halo_post where post_type='post' ORDER BY post_date DESC LIMIT 5",nativeQuery = true)
    List<Post> findTopFive();

    /**
     * 查询所有文章 根据文章类型
     *
     * @param postType post or page
     * @return List<Post></>
     */
    List<Post> findPostsByPostType(String postType);

    /**
     * 分页查询文章
     *
     * @param postType post or page
     * @param pageable 分页信息
     * @return Page<Post></>
     */
    Page<Post> findPostsByPostType(String postType,Pageable pageable);

    /**
     * 模糊查询
     *
     * @param keyWord keyword
     * @param pageable pageable
     * @return list
     */
    List<Post> findByPostTitleLike(String keyWord,Pageable pageable);

    /**
     * 根据文章的状态查询 分页
     *
     * @param status 0，1，2
     * @param postType post or page
     * @param pageable 分页信息
     * @return Page<Post></>
     */
    Page<Post> findPostsByPostStatusAndPostType(Integer status,String postType,Pageable pageable);

    /**
     * 根据文章的状态查询
     *
     * @param status 0,1,2
     * @param postType post or page
     * @return List<Post></>
     */
    List<Post> findPostsByPostStatusAndPostType(Integer status,String postType);

    /**
     * 根据路径查询文章
     *
     * @param postUrl 路径
     * @param postType post or page
     * @return Post
     */
    Post findPostByPostUrlAndPostType(String postUrl,String postType);

    /**
     * 查询之后文章
     *
     * @param postDate 发布时间
     * @param postStatus 0，1，2
     * @param postType post or page
     * @return List<Post></>
     */
    List<Post> findByPostDateAfterAndPostStatusAndPostTypeOrderByPostDateDesc(Date postDate, Integer postStatus,String postType);


    /**
     * 查询之前的文章
     *
     * @param postDate 发布时间
     * @param postStatus 0，1，2
     * @param postType post or page
     * @return List<Post></>
     */
    List<Post> findByPostDateBeforeAndPostStatusAndPostTypeOrderByPostDateAsc(Date postDate,Integer postStatus,String postType);

    /**
     * 查询文章归档信息 根据年份和月份
     *
     * @return List<Object[]></>
     */
    @Query(value = "select year(post_date) as year,month(post_date) as month,count(*) as count from halo_post where post_status=0 and post_type='post' group by year(post_date),month(post_date) order by year desc,month desc",nativeQuery = true)
    List<Object[]> findPostGroupByYearAndMonth();

    /**
     * 查询文章归档信息 根据年份
     * @return List<Object[]></>
     */
    @Query(value = "select year(post_date) as year,count(*) as count from halo_post where post_status=0 and post_type='post' group by year(post_date) order by year desc",nativeQuery = true)
    List<Object[]> findPostGroupByYear();

    /**
     * 根据年份和月份查询文章
     *
     * @param year year
     * @param month month
     * @return List<Post></>
     */
    @Query(value = "select *,year(post_date) as year,month(post_date) as month from halo_post where post_status=0 and post_type='post' and year(post_date)=:year and month(post_date)=:month order by post_date desc",nativeQuery = true)
    List<Post> findPostByYearAndMonth(@Param("year") String year,@Param("month") String month);

    /**
     * 根据年份查询文章
     *
     * @param year year
     * @return List<Post></>
     */
    @Query(value = "select *,year(post_date) as year from halo_post where post_status=0 and post_type='post' and year(post_date)=:year order by post_date desc",nativeQuery = true)
    List<Post> findPostByYear(@Param("year") String year);

    /**
     * 根据年份和月份查询文章 分页
     *
     * @param year year
     * @param month month
     * @param pageable pageable
     * @return Page<Post></>
     */
    @Query(value = "select * from halo_post where post_status=0 and post_type='post' and year(post_date)=:year and month(post_date)=:month order by post_date desc",countQuery = "select count(*) from halo_post where post_status=0 and year(post_date)=:year and month(post_date)=:month",nativeQuery = true)
    Page<Post> findPostByYearAndMonth(@Param("year") String year,@Param("month") String month,Pageable pageable);

    List<Post> findPostByCategories(Category category);

    /**
     * 根据标签查询文章
     *
     * @param tag tag
     * @param pageable pageable
     * @return page
     */
    Page<Post> findPostsByTags(Tag tag,Pageable pageable);
}
