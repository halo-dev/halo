package cc.ryanc.halo.service;

import cc.ryanc.halo.model.domain.Comment;
import cc.ryanc.halo.model.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * <pre>
 *     评论业务逻辑接口
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/1/22
 */
public interface CommentService {

    /**
     * 新增评论
     *
     * @param comment comment
     */
    void save(Comment comment);

    /**
     * 删除评论
     *
     * @param commentId commentId
     * @return Optional
     */
    Optional<Comment> remove(Long commentId);

    /**
     * 查询所有的评论，用于后台管理
     *
     * @param status   status
     * @param pageable pageable
     * @return Page
     */
    Page<Comment> findAll(Integer status, Pageable pageable);

    /**
     * 根据评论状态查询评论
     *
     * @param status 评论状态
     * @return List
     */
    List<Comment> findAll(Integer status);

    /**
     * 查询所有评论，不分页
     *
     * @return List
     */
    List<Comment> findAll();

    /**
     * 更改评论的状态
     *
     * @param commentId commentId
     * @param status    status
     * @return Comment
     */
    Comment updateCommentStatus(Long commentId, Integer status);

    /**
     * 根据评论编号查询评论
     *
     * @param commentId commentId
     * @return Optional
     */
    Optional<Comment> findCommentById(Long commentId);

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
    List<Comment> findCommentsLatest();

    /**
     * 根据评论状态查询数量
     *
     * @param status 评论状态
     * @return 评论数量
     */
    Integer getCountByStatus(Integer status);

    /**
     * 查询评论总数
     *
     * @return Long
     */
    Long getCount();

    /**
     * 获取最近的评论
     *
     * @param limit limit
     * @return List
     */
    List<Comment> getRecentComments(int limit);
}
