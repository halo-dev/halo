package cc.ryanc.halo.model.domain;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2018/1/22
 * description : 评论实体类
 */
@Data
@Entity
@Table(name = "halo_comment")
public class Comment implements Serializable {

    /**
     * 评论id 自增
     */
    @Id
    @GeneratedValue
    private Long commentId;

    /**
     * 评论文章
     */
    @ManyToOne(targetEntity = Post.class,fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id")
    private Post post;

    /**
     * 评论人
     */
    private String commentAuthor;

    /**
     * 评论人的邮箱
     */
    private String commentAuthorEmail;

    /**
     * 评论人的主页
     */
    private String commentAuthorUrl;

    /**
     * 评论人的ip
     */
    private String commentAuthorIp;

    /**
     * 评论时间
     */
    private Date commentDate;

    /**
     * 评论内容
     */
    @Lob
    private String commentContent;

    /**
     * 评论者ua信息
     */
    private String commentAgent;

    /**
     * 上一级
     */
    @OneToOne
    @JoinColumn(name = "comment_id")
    private Comment commentParent;

    /**
     * 评论状态，0：正常，1：待审核，2：回收站
     */
    private Integer commentStatus=1;
}
