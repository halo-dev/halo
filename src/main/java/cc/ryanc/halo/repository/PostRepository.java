package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.domain.Category;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.dto.Archive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2017/11/14
 * description : 文章持久层
 */
public interface PostRepository extends JpaRepository<Post,Integer>{

    /**
     * 查询前五条文章
     * @return list
     */
    @Query(value = "SELECT * FROM halo_post ORDER BY post_id DESC LIMIT 5",nativeQuery = true)
    List<Post> findTopFive();

    /**
     * 分页查询文章
     * @param pageable pageable
     * @return page
     */
    @Override
    Page<Post> findAll(Pageable pageable);

    /**
     * 模糊查询
     * @param keyWord keyword
     * @param pageable pageable
     * @return list
     */
    List<Post> findByPostTitleLike(String keyWord,Pageable pageable);

    /**
     * 根据文章的状态查询 分页
     * @param status status
     * @param pageable pageable
     * @return page
     */
    Page<Post> findPostsByPostStatus(Integer status,Pageable pageable);

    /**
     * 根据文章的状态查询
     * @param status status
     * @return List
     */
    List<Post> findPostsByPostStatus(Integer status);

    /**
     * 根据路径查询文章
     * @param postUrl postUrl
     * @return Post
     */
    Post findPostByPostUrl(String postUrl);

    /**
     * 查询之后文章
     * @param postDate postDate
     * @param postStatus postStatus
     * @return list
     */
    List<Post> findByPostDateAfterAndPostStatusOrderByPostDateDesc(Date postDate, Integer postStatus);


    /**
     * 查询之前的文章
     * @param postDate postDate
     * @param postStatus postStatus
     * @return list
     */
    List<Post> findByPostDateBeforeAndPostStatusOrderByPostDateAsc(Date postDate,Integer postStatus);

    /**
     * 查询文章归档信息
     * @return list
     */
    @Query(value = "select year(post_date) as year,month(post_date) as month,count(*) as count from halo_post where post_status=0 group by year(post_date) DESC ,month(post_date) DESC",nativeQuery = true)
    List<Object[]> findPostGroupByDate();

    /**
     * 根据年份和月份查询文章
     * @param year year
     * @param month month
     * @return list
     */
    @Query(value = "select *,year(post_date) as year,month(post_date) as month from halo_post where post_status=0 and year(post_date)=:year and month(post_date)=:month order by post_date desc",nativeQuery = true)
    List<Post> findPostByYearAndMonth(@Param("year") String year,@Param("month") String month);

    /**
     * 根据年份和月份查询文章 分页
     * @param year year
     * @param month month
     * @param pageable pageable
     * @return page
     */
    @Query(value = "select * from halo_post where post_status=0 and year(post_date)=:year and month(post_date)=:month order by ?#{#pageable}",countQuery = "select * from halo_post",nativeQuery = true)
    Page<Post> findPostByYearAndMonth(@Param("year") String year,@Param("month") String month,Pageable pageable);

    List<Post> findPostByCategories(Category category);
}
