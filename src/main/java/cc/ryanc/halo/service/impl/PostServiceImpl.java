package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.dto.Archive;
import cc.ryanc.halo.repository.PostRepository;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.util.HaloUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author : RYAN0UP
 * @date : 2017/11/14
 * @version : 1.0
 * description:
 */
@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    private static final String POST_KEY = "'post_key'";

    private static final String POST_CACHE_NAME = "inkCache";

    /**
     * 保存文章 清除缓存
     * @param post Post
     * @return Post
     */
    @CacheEvict(value = POST_CACHE_NAME,key = POST_KEY)
    @Override
    public Post saveByPost(Post post) {
        return postRepository.save(post);
    }

    /**
     * 根据编号移除文章 清除缓存
     * @param postId postId
     * @return Post
     */
    @CacheEvict(value = POST_CACHE_NAME,key = POST_KEY)
    @Override
    public Post removeByPostId(Long postId) {
        Post post = this.findByPostId(postId);
        postRepository.delete(post);
        return post;
    }

    /**
     * 修改文章 清除缓存
     * @param post Post
     * @return
     */
    @CacheEvict(value = POST_CACHE_NAME,key = POST_KEY)
    @Override
    public Post updateByPost(Post post) {
        return postRepository.save(post);
    }

    /**
     * 修改文章状态 清除缓存
     * @param postId postId
     * @param status status
     * @return Post
     */
    @CacheEvict(value = POST_CACHE_NAME,key = POST_KEY)
    @Override
    public Post updatePostStatus(Long postId, Integer status) {
        Post post = this.findByPostId(postId);
        post.setPostStatus(status);
        return postRepository.save(post);
    }

    /**
     * 批量更新文章摘要
     * @param postSummary postSummary
     */
    @CacheEvict(value = POST_CACHE_NAME,key = POST_KEY)
    @Override
    public void updateAllSummary(Integer postSummary) {
        List<Post> posts = this.findAllPosts();
        for(Post post:posts){
            if(!(HaloUtil.htmlToText(post.getPostContent()).length()<postSummary)){
                post.setPostSummary(HaloUtil.getSummary(post.getPostContent(),postSummary));
                postRepository.save(post);
            }
        }
    }

    /**
     * 查询所有文章 分页
     * @param pageable Pageable
     * @return Page
     */
    @Override
    public Page<Post> findAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    /**
     * 查询所有文章 不分页 缓存
     * @return List
     */
    @Cacheable(value = POST_CACHE_NAME,key = POST_KEY)
    @Override
    public List<Post> findAllPosts() {
        return postRepository.findAll();
    }

    /**
     * 模糊查询文章
     * @param keyWord keyword
     * @param pageable pageable
     * @return list
     */
    @Override
    public List<Post> searchPosts(String keyWord,Pageable pageable) {
        return postRepository.findByPostTitleLike(keyWord,pageable);
    }

    /**
     * 根据状态分页查询文章 清除缓存
     * @param status status
     * @param pageable pageable
     * @return page
     */
    @CacheEvict(value = POST_CACHE_NAME,key = POST_KEY)
    @Override
    public Page<Post> findPostByStatus(Integer status, Pageable pageable) {
        return postRepository.findPostsByPostStatus(status,pageable);
    }

    /**
     * 根据状态查询文章
     * @param status status
     * @return list
     */
    @Override
    public List<Post> findPostByStatus(Integer status) {
        return postRepository.findPostsByPostStatus(status);
    }

    /**
     * 根据编号查询文章 缓存
     * @param postId postId
     * @return post
     */
    @Cacheable(value = POST_CACHE_NAME,key = "#postId+'post'")
    @Override
    public Post findByPostId(Long postId) {
        return postRepository.findOne(postId);
    }

    /**
     * 根据文章路径查询 缓存
     * @param postUrl postUrl
     * @return post
     */
    @Override
    @Cacheable(value = POST_CACHE_NAME,key = "#postUrl+'post'")
    public Post findByPostUrl(String postUrl) {
        return postRepository.findPostByPostUrl(postUrl);
    }

    /**
     * 查询最新的5篇文章
     * @return list
     */
    @Override
    public List<Post> findPostLatest() {
        return postRepository.findTopFive();
    }

    /**
     * 查询Id之后的文章
     *
     * @param postId postId
     * @return post
     */
    @Override
    public List<Post> findByPostDateAfter(Date postDate) {
        return postRepository.findByPostDateAfterAndPostStatusOrderByPostDateDesc(postDate,0);
    }

    /**
     * 查询Id之前的文章
     *
     * @param postId
     * @return
     */
    @Override
    public List<Post> findByPostDateBefore(Date postDate) {
        return postRepository.findByPostDateBeforeAndPostStatusOrderByPostDateAsc(postDate,0);
    }


    /**
     * 查询归档信息
     *
     * @return List
     */
    @Override
    public List<Archive> findPostGroupByPostDate() {
        List<Object[]> objects = postRepository.findPostGroupByDate();
        List<Archive> archives = new ArrayList<>();
        Archive archive = null;
        for(Object[] obj : objects){
            archive = new Archive();
            archive.setYear(obj[0].toString());
            archive.setMonth(obj[1].toString());
            archive.setCount(obj[2].toString());
            archive.setPosts(this.findPostByYearAndMonth(obj[0].toString(),obj[1].toString()));
            archives.add(archive);
        }
        return archives;
    }

    /**
     * 根据年份和月份查询文章
     * @param year year
     * @param month month
     * @return list
     */
    @Override
    public List<Post> findPostByYearAndMonth(String year, String month) {
        return postRepository.findPostByYearAndMonth(year,month);
    }

    @Override
    public Page<Post> findPostByYearAndMonth(String year, String month, Pageable pageable) {
        return postRepository.findPostByYearAndMonth(year,month,pageable);
    }
}
