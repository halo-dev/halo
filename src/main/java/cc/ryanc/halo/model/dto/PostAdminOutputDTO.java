package cc.ryanc.halo.model.dto;

import cc.ryanc.halo.model.domain.Category;
import cc.ryanc.halo.model.domain.Comment;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.domain.Tag;
import cc.ryanc.halo.model.dto.base.AbstractOutputConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Post admin output dto.
 *
 * @author johnniang
 */
@Data
public class PostAdminOutputDTO extends AbstractOutputConverter<PostAdminOutputDTO, Post> {

    private Long postId;

    private String postTitle;

    private String postType;

    private String postUrl;

    private List<Category> categories;

    private List<Tag> tags;

    private List<Comment> comments;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date postDate;

    private Integer postStatus;

    private Long postViews;

    private Integer postPriority;
}
