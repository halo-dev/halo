package cc.ryanc.halo.model.dto;

import cc.ryanc.halo.model.domain.Comment;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.dto.base.OutputConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author : RYAN0UP
 * @date : 2019-03-09
 */
@Data
public class CommentAdminOutputDTO implements OutputConverter<CommentAdminOutputDTO, Comment> {

    private Long commentId;

    private Post post;

    private String commentAuthor;

    private String commentAuthorUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date commentDate;

    private String commentContent;

    private Integer commentStatus;

    private Integer isAdmin;
}
