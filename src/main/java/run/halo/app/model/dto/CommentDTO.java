package run.halo.app.model.dto;

import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.Comment;
import run.halo.app.model.enums.CommentStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

/**
 * Comment output dto.
 *
 * @author johnniang
 */
@Data
@ToString
@EqualsAndHashCode
public class CommentDTO implements OutputConverter<CommentDTO, Comment> {

    private Long id;

    private String author;

    private String email;

    private String ipAddress;

    private String gavatarMd5;

    private String content;

    private CommentStatus status;

    private String userAgent;

    private Long parentId;

    private Boolean isAdmin;

    private Date createTime;

}
