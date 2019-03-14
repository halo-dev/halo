package cc.ryanc.halo.model.dto;

import cc.ryanc.halo.model.dto.base.OutputConverter;
import cc.ryanc.halo.model.entity.Comment;
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
public class CommentOutputDTO implements OutputConverter<CommentOutputDTO, Comment> {

    private Long id;

    private String author;

    private String email;

    private String ipAddress;

    private String gavatarMd5;

    private String content;

    private String userAgent;

    private Long parentId;

    private Boolean isAdmin;

    private Date createTime;

}
