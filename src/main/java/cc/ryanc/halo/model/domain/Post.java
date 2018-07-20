package cc.ryanc.halo.model.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <pre>
 *     文章／页面
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/11/14
 */
@Data
@Entity
@Table(name = "halo_post")
public class Post implements Serializable {

    private static final long serialVersionUID = -6019684584665869629L;

    /**
     * 文章编号
     */
    @Id
    @GeneratedValue
    private Long postId;

    /**
     * 发表用户 多对一
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 文章标题
     */
    private String postTitle;

    /**
     * 文章类型
     * post  文章
     * page  页面
     */
    private String postType = "post";

    /**
     * 文章内容 Markdown格式
     */
    @Lob
    private String postContentMd;

    /**
     * 文章内容 html格式
     */
    @Lob
    private String postContent;

    /**
     * 文章路径
     */
    @Column(unique = true)
    private String postUrl;

    /**
     * 文章摘要
     */
    private String postSummary;

    /**
     * 文章所属分类
     */
    @ManyToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinTable(name = "halo_posts_categories",
            joinColumns = {@JoinColumn(name = "post_id", nullable = false)},
            inverseJoinColumns = {@JoinColumn(name = "cate_id", nullable = false)})
    private List<Category> categories = new ArrayList<>();

    /**
     * 文章所属标签
     */
    @ManyToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinTable(name = "halo_posts_tags",
            joinColumns = {@JoinColumn(name = "post_id", nullable = false)},
            inverseJoinColumns = {@JoinColumn(name = "tag_id", nullable = false)})
    private List<Tag> tags = new ArrayList<>();

    /**
     * 文章的评论
     */
    @OneToMany(mappedBy = "post", cascade = {CascadeType.REMOVE}, fetch = FetchType.EAGER)
    private List<Comment> comments = new ArrayList<>();

    /**
     * 缩略图
     */
    private String postThumbnail;

    /**
     * 发表日期
     */
    private Date postDate;

    /**
     * 最后一次更新时间
     */
    private Date postUpdate;

    /**
     * 0 已发布
     * 1 草稿
     * 2 回收站
     */
    private Integer postStatus = 0;

    /**
     * 文章访问量
     */
    private Long postViews = 0L;

    /**
     * 是否允许评论
     */
    private Integer allowComment = 0;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    public Date getPostDate() {
        return postDate;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    public Date getPostUpdate() {
        return postUpdate;
    }
}
