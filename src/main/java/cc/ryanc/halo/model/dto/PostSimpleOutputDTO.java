package cc.ryanc.halo.model.dto;

import cc.ryanc.halo.model.domain.Category;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.domain.Tag;
import cc.ryanc.halo.model.dto.base.OutputConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Post output dto without markdown and content.
 *
 * @author johnniang
 */
@Data
public class PostSimpleOutputDTO implements OutputConverter<PostSimpleOutputDTO, Post> {

    private Long postId;

    private String postTitle;

    private String postType;

    private String postUrl;

    private String postSummary;

    private List<Category> categories;

    private List<Tag> tags;

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
