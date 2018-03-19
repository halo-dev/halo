package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.domain.Comment;
import cc.ryanc.halo.model.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * description :
 * @date : 2018/1/22
 */
public interface CommentRepository extends JpaRepository<Comment,Long> {

    /**
     * 根据评论状态查询所有评论
     *
     * @param status Status 文章状态
     * @param pageable pageable 分页信息
     * @return page
     */
    Page<Comment> findCommentsByCommentStatus(Integer status, Pageable pageable);

    /**
     * 根据文章查询评论
     *
     * @param post post
     * @param pageable pageable
     * @return page
     */
    Page<Comment> findCommentsByPost(Post post,Pageable pageable);

    /**
     * 根据文章和评论状态查询评论
     *
     * @param post post
     * @param pageable pageable
     * @param status status
     * @return page
     */
    Page<Comment> findCommentsByPostAndCommentStatusNot(Post post,Pageable pageable,Integer status);

    /**
     * 查询最新的前五条评论
     *
     * @return list
     */
    @Query(value = "SELECT * FROM halo_comment ORDER BY comment_date DESC LIMIT 5",nativeQuery = true)
    List<Comment> findTopFive();

}
