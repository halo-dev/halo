package cc.ryanc.halo.model.dto;

import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.dto.base.AbstractOutputConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author : RYAN0UP
 * @date : 2019-03-09
 */
@Data
public class PostViewOutputDTO extends AbstractOutputConverter<PostViewOutputDTO, Post> {

    private Long postId;

    private String postTitle;

    private String postUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date postDate;

    private Integer postStatus;
}
