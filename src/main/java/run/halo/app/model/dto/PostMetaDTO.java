package run.halo.app.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.PostMeta;

import java.util.Date;

/**
 * Post meta output dto.
 *
 * @author guqing
 * @date 2019-11-30
 */
@Data
@ToString
@EqualsAndHashCode
public class PostMetaDTO implements OutputConverter<PostMetaDTO, PostMeta> {
    private Long id;

    private Integer postId;

    private String key;

    private String value;

    private Date createTime;
}
