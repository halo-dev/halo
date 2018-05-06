package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.domain.Category;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.domain.Tag;
import cc.ryanc.halo.model.dto.Archive;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.repository.PostRepository;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.util.HaloUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2017/11/14
 */
@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    /**
     * 保存文章
     *
     * @param post Post
     * @return Post
     */
    @Override
    public Post saveByPost(Post post) {
        return postRepository.save(post);
    }

    /**
     * 根据编号移除文章
     *
     * @param postId postId
     * @return Post
     */
    @Override
    public Post removeByPostId(Long postId) {
        Optional<Post> post = this.findByPostId(postId);
        postRepository.delete(post.get());
        return post.get();
    }

    /**
     * 修改文章状态
     *
     * @param postId postId
     * @param status status
     * @return Post
     */
    @Override
    public Post updatePostStatus(Long postId, Integer status) {
        Optional<Post> post = this.findByPostId(postId);
        post.get().setPostStatus(status);
        return postRepository.save(post.get());
    }

    /**
     * 批量更新文章摘要
     *
     * @param postSummary postSummary
     */
    @Override
    public void updateAllSummary(Integer postSummary) {
        List<Post> posts = this.findAllPosts(HaloConst.POST_TYPE_POST);
        for (Post post : posts) {
            if (!(HaloUtil.htmlToText(post.getPostContent()).length() < postSummary)) {
                post.setPostSummary(HaloUtil.getSummary(post.getPostContent(), postSummary));
            } else {
                post.setPostSummary(HaloUtil.htmlToText(post.getPostContent()));
            }
            postRepository.save(post);
        }
    }

    /**
     * 获取文章列表 分页
     *
     * @param postType post or page
     * @param pageable 分页信息
     * @return Page<Post></>
     */
    @Override
    public Page<Post> findAllPosts(String postType, Pageable pageable) {
        return postRepository.findPostsByPostType(postType, pageable);
    }

    /**
     * 获取文章列表 不分页
     *
     * @param postType post or page
     * @return List<Post></>
     */
    @Override
    public List<Post> findAllPosts(String postType) {
        return postRepository.findPostsByPostType(postType);
    }

    /**
     * 模糊查询文章
     *
     * @param keyWord  keyword
     * @param pageable pageable
     * @return list
     */
    @Override
    public List<Post> searchPosts(String keyWord, Pageable pageable) {
        return postRepository.findByPostTitleLike(keyWord, pageable);
    }

    /**
     * 根据文章状态查询 分页
     *
     * @param status   0，1，2
     * @param postType post or page
     * @param pageable 分页信息
     * @return Page<Post></>
     */
    @Override
    public Page<Post> findPostByStatus(Integer status, String postType, Pageable pageable) {
        return postRepository.findPostsByPostStatusAndPostType(status, postType, pageable);
    }

    /**
     * 根据文章状态查询
     *
     * @param status   0，1，2
     * @param postType post or page
     * @return List<Post></>
     */
    @Override
    public List<Post> findPostByStatus(Integer status, String postType) {
        return postRepository.findPostsByPostStatusAndPostType(status, postType);
    }

    /**
     * 根据编号查询文章
     *
     * @param postId postId
     * @return post
     */
    @Override
    public Optional<Post> findByPostId(Long postId) {
        return postRepository.findById(postId);
    }

    /**
     * 根据文章路径查询
     *
     * @param postUrl  路径
     * @param postType post or page
     * @return Post
     */
    @Override
    public Post findByPostUrl(String postUrl, String postType) {
        return postRepository.findPostByPostUrlAndPostType(postUrl, postType);
    }

    /**
     * 查询最新的5篇文章
     *
     * @return list
     */
    @Override
    public List<Post> findPostLatest() {
        return postRepository.findTopFive();
    }

    /**
     * 查询之后的文章
     *
     * @param postDate 发布时间
     * @return List
     */
    @Override
    public List<Post> findByPostDateAfter(Date postDate) {
        return postRepository.findByPostDateAfterAndPostStatusAndPostTypeOrderByPostDateDesc(postDate, 0, HaloConst.POST_TYPE_POST);
    }

    /**
     * 查询Id之前的文章
     *
     * @param postDate 发布时间
     * @return list
     */
    @Override
    public List<Post> findByPostDateBefore(Date postDate) {
        return postRepository.findByPostDateBeforeAndPostStatusAndPostTypeOrderByPostDateAsc(postDate, 0, HaloConst.POST_TYPE_POST);
    }


    /**
     * 查询归档信息 根据年份和月份
     *
     * @return List
     */
    @Override
    public List<Archive> findPostGroupByYearAndMonth() {
        List<Object[]> objects = postRepository.findPostGroupByYearAndMonth();
        List<Archive> archives = new ArrayList<>();
        Archive archive = null;
        for (Object[] obj : objects) {
            archive = new Archive();
            archive.setYear(obj[0].toString());
            archive.setMonth(obj[1].toString());
            archive.setCount(obj[2].toString());
            archive.setPosts(this.findPostByYearAndMonth(obj[0].toString(), obj[1].toString()));
            archives.add(archive);
        }
        return archives;
    }

    /**
     * 查询归档信息 根据年份
     *
     * @return list
     */
    @Override
    public List<Archive> findPostGroupByYear() {
        List<Object[]> objects = postRepository.findPostGroupByYear();
        List<Archive> archives = new ArrayList<>();
        Archive archive = null;
        for (Object[] obj : objects) {
            archive = new Archive();
            archive.setYear(obj[0].toString());
            archive.setCount(obj[1].toString());
            archive.setPosts(this.findPostByYear(obj[0].toString()));
            archives.add(archive);
        }
        return archives;
    }

    /**
     * 根据年份和月份查询文章
     *
     * @param year  year
     * @param month month
     * @return list
     */
    @Override
    public List<Post> findPostByYearAndMonth(String year, String month) {
        return postRepository.findPostByYearAndMonth(year, month);
    }

    /**
     * 根据年份查询文章
     *
     * @param year year
     * @return list
     */
    @Override
    public List<Post> findPostByYear(String year) {
        return postRepository.findPostByYear(year);
    }

    /**
     * 根据年份和月份索引文章
     *
     * @param year     year year
     * @param month    month month
     * @param pageable pageable pageable
     * @return page
     */
    @Override
    public Page<Post> findPostByYearAndMonth(String year, String month, Pageable pageable) {
        return postRepository.findPostByYearAndMonth(year, month, null);
    }

    /**
     * 根据分类目录查询文章
     *
     * @param category category
     * @param pageable pageable
     * @return Page<Post></>
     */
    @Override
    public Page<Post> findPostByCategories(Category category, Pageable pageable) {
        return postRepository.findPostByCategories(category,pageable);
    }

    /**
     * 根据标签查询文章
     *
     * @param tag      tag
     * @param pageable pageable
     * @return page
     */
    @Override
    public Page<Post> findPostsByTags(Tag tag, Pageable pageable) {
        return postRepository.findPostsByTags(tag, pageable);
    }

    /**
     * 搜索文章
     *
     * @param keyword 关键词
     * @param pageable 分页信息
     * @return List<Post></>
     */
    @Override
    public Page<Post> searchByKeywords(String keyword,Pageable pageable) {
        return postRepository.findPostByPostTitleLikeOrPostContentLikeAndPostTypeAndPostStatus(keyword,pageable);
    }

    /**
     * 生成rss
     *
     * @param posts posts
     * @return string
     */
    @Override
    public String buildRss(List<Post> posts) {
        String rss = "";
        try {
            rss = HaloUtil.getRss(posts);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rss;
    }

    /**
     * 生成sitemap
     *
     * @param posts posts
     * @return string
     */
    @Override
    public String buildSiteMap(List<Post> posts) {
        return HaloUtil.getSiteMap(posts);
    }
}
