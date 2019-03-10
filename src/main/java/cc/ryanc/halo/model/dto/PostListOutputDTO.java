package cc.ryanc.halo.model.dto;

import cc.ryanc.halo.model.domain.*;
import cc.ryanc.halo.model.dto.base.AbstractOutputConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Post output dto for post list
 *
 * @author johnniang
 */
@Data
public class PostListOutputDTO extends AbstractOutputConverter<PostListOutputDTO, Post> {

    private Long postId;

    private User user;

    private String postTitle;

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

    private Long postViews;

    private Integer allowComment;

    private Integer postPriority;
}
