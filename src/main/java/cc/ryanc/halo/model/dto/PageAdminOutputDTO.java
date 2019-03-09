package cc.ryanc.halo.model.dto;

import cc.ryanc.halo.model.domain.Comment;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.dto.base.AbstractOutputConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author : RYAN0UP
 * @date : 2019-03-09
 */
@Data
public class PageAdminOutputDTO extends AbstractOutputConverter<PageAdminOutputDTO, Post> {

    private Long postId;

    private String postTitle;

    private String postType;

    private String postUrl;

    private List<Comment> comments;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date postDate;

    private Integer postStatus;

    private Long postViews;
}
