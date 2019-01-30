package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.domain.Comment;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.repository.CommentRepository;
import cc.ryanc.halo.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * <pre>
 *     评论业务逻辑实现类
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/1/22
 */
@Service
public class CommentServiceImpl implements CommentService {

    private static final String COMMENTS_CACHE_NAME = "comments";

    private static final String POSTS_CACHE_NAME = "posts";

    @Autowired
    private CommentRepository commentRepository;

    /**
     * 新增评论
     *
     * @param comment comment
     */
    @Override
    @CacheEvict(value = {COMMENTS_CACHE_NAME, POSTS_CACHE_NAME}, allEntries = true, beforeInvocation = true)
    public void save(Comment comment) {
        commentRepository.save(comment);
    }

    /**
     * 删除评论
     *
     * @param commentId commentId
     * @return Optional
     */
    @Override
    @CacheEvict(value = {COMMENTS_CACHE_NAME, POSTS_CACHE_NAME}, allEntries = true, beforeInvocation = true)
    public Optional<Comment> remove(Long commentId) {
        final Optional<Comment> comment = this.findCommentById(commentId);
        commentRepository.delete(comment.orElse(null));
        return comment;
    }

    /**
     * 查询所有的评论，用于后台管理
     *
     * @param pageable pageable
     * @return Page
     */
    @Override
    public Page<Comment> findAll(Integer status, Pageable pageable) {
        return commentRepository.findCommentsByCommentStatus(status, pageable);
    }

    /**
     * 根据评论状态查询评论
     *
     * @param status 评论状态
     * @return List
     */
    @Override
    @CachePut(value = COMMENTS_CACHE_NAME, key = "'comments_status_'+#status")
    public List<Comment> findAll(Integer status) {
        return commentRepository.findCommentsByCommentStatus(status);
    }

    /**
     * 查询所有评论，不分页
     *
     * @return List<Comment></>
     */
    @Override
    @Cacheable(value = COMMENTS_CACHE_NAME, key = "'comment'")
    public List<Comment> findAll() {
        return commentRepository.findAll();
    }

    /**
     * 更改评论的状态
     *
     * @param commentId commentId
     * @param status    status
     * @return Comment
     */
    @Override
    @CacheEvict(value = COMMENTS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public Comment updateCommentStatus(Long commentId, Integer status) {
        final Optional<Comment> comment = findCommentById(commentId);
        comment.get().setCommentStatus(status);
        return commentRepository.save(comment.get());
    }

    /**
     * 根据评论编号查询评论
     *
     * @param commentId commentId
     * @return Optional
     */
    @Override
    public Optional<Comment> findCommentById(Long commentId) {
        return commentRepository.findById(commentId);
    }

    /**
     * 根据文章查询评论
     *
     * @param post     post
     * @param pageable pageable
     * @return Page
     */
    @Override
    public Page<Comment> findCommentsByPost(Post post, Pageable pageable) {
        return commentRepository.findCommentsByPost(post, pageable);
    }

    /**
     * 根据文章和评论状态查询评论
     *
     * @param post     post
     * @param pageable pageable
     * @param status   status
     * @return Page
     */
    @Override
    public Page<Comment> findCommentsByPostAndCommentStatus(Post post, Pageable pageable, Integer status) {
        return commentRepository.findCommentsByPostAndCommentStatus(post, pageable, status);
    }

    /**
     * 根据文章和评论状态查询评论 不分页
     *
     * @param post   post
     * @param status status
     * @return List
     */
    @Override
    public List<Comment> findCommentsByPostAndCommentStatus(Post post, Integer status) {
        return commentRepository.findCommentsByPostAndCommentStatus(post, status);
    }

    /**
     * 根据文章和评论状态（为不查询的）查询评论 不分页
     *
     * @param post   post
     * @param status status
     * @return List
     */
    @Override
    public List<Comment> findCommentsByPostAndCommentStatusNot(Post post, Integer status) {
        return commentRepository.findCommentsByPostAndCommentStatusNot(post, status);
    }

    /**
     * 查询最新的前五条评论
     *
     * @return List
     */
    @Override
    @Cacheable(value = COMMENTS_CACHE_NAME, key = "'comments_latest'")
    public List<Comment> findCommentsLatest() {
        return commentRepository.findTopFive();
    }

    /**
     * 根据评论状态查询数量
     *
     * @param status 评论状态
     * @return 评论数量
     */
    @Override
    public Integer getCountByStatus(Integer status) {
        return commentRepository.countAllByCommentStatus(status);
    }

    /**
     * 查询评论总数
     *
     * @return Long
     */
    @Override
    public Long getCount() {
        return commentRepository.count();
    }

    /**
     * 获取最近的评论
     *
     * @param limit limit
     * @return List
     */
    @Override
    public List<Comment> getRecentComments(int limit) {
        return commentRepository.getCommentsByLimit(limit);
    }
}
