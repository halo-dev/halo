package run.halo.app.model.dto;

import lombok.Data;
import lombok.ToString;
import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.Email;

/**
 * Email with post count output dto.
 *
 * @author johnniang
 * @date 3/20/19
 */
@Data
@ToString(callSuper = true)
public class PostEmailDTO implements OutputConverter<PostEmailDTO, Email> {
    private Integer id;

    private Integer emailId;

    private String emailName;

    private String emailValue;

    private Integer postId;

    private String postTitle;

    private String postSlug;

    private String postFormatContent;

    private String postOriginalContent;


    private Long postCount;

    private String emailFullPath;
}
