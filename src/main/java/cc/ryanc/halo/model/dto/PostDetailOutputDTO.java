package cc.ryanc.halo.model.dto;

import cc.ryanc.halo.model.domain.*;
import cc.ryanc.halo.model.dto.base.AbstractOutputConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Post detail output dto.
 *
 * @author johnniang
 */
@Data
public class PostDetailOutputDTO extends AbstractOutputConverter<PostDetailOutputDTO, Post> {

    private Long postId;

    private User user;

    private String postTitle;

    private String postType;

    private String postContent;

    private String postUrl;

    private String postSummary;

    private List<Category> categories;

    private List<Tag> tags;

    private List<Comment> comments;

    private String postThumbnail;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date postDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date postUpdate;

    private Integer postStatus;

    private Long postViews;

    private Integer allowComment;

    private String postPassword;

    private String customTpl;

    private Integer postPriority;

}
