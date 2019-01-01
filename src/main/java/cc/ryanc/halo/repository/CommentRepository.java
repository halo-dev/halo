package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.domain.Comment;
import cc.ryanc.halo.model.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * <pre>
 *     评论持久层
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/1/22
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 根据评论状态查询所有评论 分页
     *
     * @param status   文章状态
     * @param pageable 分页信息
     * @return Page
     */
    Page<Comment> findCommentsByCommentStatus(Integer status, Pageable pageable);

    /**
     * 根据评论状态查询所有评论 不分页
     *
     * @param status 文章状态
     * @return List
     */
    List<Comment> findCommentsByCommentStatus(Integer status);

    /**
     * 根据文章查询评论
     *
     * @param post     post
     * @param pageable pageable
     * @return Page
     */
    Page<Comment> findCommentsByPost(Post post, Pageable pageable);

    /**
     * 根据文章和评论状态查询评论 分页
     *
     * @param post     post
     * @param pageable pageable
     * @param status   status
     * @return Page
     */
    Page<Comment> findCommentsByPostAndCommentStatus(Post post, Pageable pageable, Integer status);

    /**
     * 根据文章和评论状态查询评论 不分页
     *
     * @param post   post
     * @param status status
     * @return List
     */
    List<Comment> findCommentsByPostAndCommentStatus(Post post, Integer status);

    /**
     * 根据文章和评论状态（为不查询的）查询评论 不分页
     *
     * @param post   post
     * @param status status
     * @return List
     */
    List<Comment> findCommentsByPostAndCommentStatusNot(Post post, Integer status);

    /**
     * 查询最新的前五条评论
     *
     * @return List
     */
    @Query(value = "SELECT * FROM halo_comment ORDER BY comment_date DESC LIMIT 5", nativeQuery = true)
    List<Comment> findTopFive();

    /**
     * 根据评论状态查询数量
     *
     * @param status 评论状态
     * @return 评论数量
     */
    Integer countAllByCommentStatus(Integer status);

    /**
     * 获取指定条数的评论
     *
     * @param limit 条数
     * @return List
     */
    @Query(value = "SELECT * FROM halo_comment WHERE comment_status = 0 ORDER BY comment_date DESC LIMIT :limit",nativeQuery = true)
    List<Comment> getCommentsByLimit(@Param(value = "limit") int limit);
}
