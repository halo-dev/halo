package cc.ryanc.halo.service;

import cc.ryanc.halo.model.domain.Comment;
import cc.ryanc.halo.model.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2018/1/22
 * description :
 */
public interface CommentService {

    /**
     * 新增评论
     *
     * @param comment comment
     */
    void saveByComment(Comment comment);

    /**
     * 删除评论
     *
     * @param commentId commentId
     * @return comment
     */
    Optional<Comment> removeByCommentId(Long commentId);

    /**
     * 查询所有的评论，用于后台管理
     *
     * @param status status
     * @param pageable pageable
     * @return page
     */
    Page<Comment> findAllComments(Integer status, Pageable pageable);


    /**
     * 查询所有评论，不分页
     *
     * @return list
     */
    List<Comment> findAllComments();

    /**
     * 更改评论的状态
     *
     * @param commentId commentId
     * @param status status
     * @return comment
     */
    Optional<Comment> updateCommentStatus(Long commentId,Integer status);

    /**
     * 根据评论编号查询评论
     *
     * @param commentId commentId
     * @return comment
     */
    Optional<Comment> findCommentById(Long commentId);

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
    Page<Comment> findCommentsByPostAndCommentStatus(Post post,Pageable pageable,Integer status);

    /**
     * 查询最新的前五条评论
     *
     * @return list
     */
    List<Comment> findCommentsLatest();
}
