package run.halo.app.model.params;

import cn.hutool.crypto.digest.BCrypt;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.model.entity.Post;
import run.halo.app.model.enums.PostCreateFrom;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.utils.HaloUtils;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.Set;

/**
 * Post param.
 *
 * @author johnniang
 * @date 3/21/19
 */
@Data
public class PostParam implements InputConverter<Post> {

    @NotBlank(message = "文章标题不能为空")
    @Size(max = 100, message = "文章标题的字符长度不能超过 {max}")
    private String title;

    private PostStatus status = PostStatus.DRAFT;

    private String url;

    @NotBlank(message = "文章内容不能为空")
    private String originalContent;

    private String summary;

    @Size(max = 255, message = "文章缩略图链接的字符长度不能超过 {max}")
    private String thumbnail;

    private Boolean disallowComment = false;

    @Size(max = 255, message = "文章密码的字符长度不能超过 {max}")
    private String password;

    @Size(max = 255, message = "Length of template must not be more than {max}")
    private String template;

    @Min(value = 0, message = "Post top priority must not be less than {value}")
    private Integer topPriority = 0;

    private Date createTime;

    private PostCreateFrom createFrom = PostCreateFrom.ADMIN;

    private Set<Integer> tagIds;

    private Set<Integer> categoryIds;

    @Override
    public Post convertTo() {
        if (StringUtils.isBlank(url)) {
            url = title;
        }

        Post post = InputConverter.super.convertTo();
        // Crypt password
        if (StringUtils.isNotBlank(password)) {
            post.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        }

        return post;
    }

    @Override
    public void update(Post post) {
        if (StringUtils.isBlank(url)) {
            url = title;
        }

        InputConverter.super.update(post);

        // Crypt password
        if (StringUtils.isNotBlank(password)) {
            post.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        }
    }
}
