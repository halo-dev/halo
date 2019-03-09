package cc.ryanc.halo.model.dto;

import cc.ryanc.halo.model.domain.Comment;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.dto.base.AbstractOutputConverter;
import lombok.Data;

import java.util.Date;

/**
 * @author : RYAN0UP
 * @date : 2019-03-09
 */
@Data
public class CommentViewOutputDTO extends AbstractOutputConverter<CommentViewOutputDTO, Comment> {

    private Long commentId;

    private Post post;

    private String commentAuthor;

    private Date commentDate;

    private String commentContent;

    private Integer commentStatus;
}
