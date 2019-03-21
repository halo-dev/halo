package cc.ryanc.halo.model.params;

import cc.ryanc.halo.model.dto.base.InputConverter;
import cc.ryanc.halo.model.entity.Post;
import cc.ryanc.halo.model.enums.PostCreateFrom;
import cc.ryanc.halo.model.enums.PostStatus;
import cc.ryanc.halo.utils.HaloUtils;
import cn.hutool.crypto.digest.BCrypt;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

/**
 * Post param.
 *
 * @author johnniang
 * @date 3/21/19
 */
@Data
public class PostParam implements InputConverter<Post> {

    @NotBlank(message = "Post title must not be blank")
    @Size(max = 100, message = "Length of post title must not be more than {max}")
    private String title;

    private PostStatus status = PostStatus.DRAFT;

    private String url;

    @NotBlank(message = "Post original content must not be blank")
    private String originalContent;

    @Size(max = 255, message = "Length of post thumbnail must not be more than {max}")
    private String thumbnail;

    private Boolean disallowComment = false;

    @Size(max = 255, message = "Length of post password must not be more than {max}")
    private String password;

    @Size(max = 255, message = "Length of post template must not be more than {max}")
    private String template;

    @Min(value = 0, message = "Post top priority must not be less than {value}")
    private Integer topPriority = 0;

    private PostCreateFrom createFrom = PostCreateFrom.ADMIN;

    private Set<Integer> tagIds;

    private Set<Integer> categoryIds;

    @Override
    public Post convertTo() {
        if (StringUtils.isBlank(url)) {
            url = HaloUtils.normalizeUrl(title);
        } else {
            url = HaloUtils.normalizeUrl(url);
        }

        url = HaloUtils.initializeUrlIfBlank(url);

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
            url = HaloUtils.normalizeUrl(title);
        } else {
            url = HaloUtils.normalizeUrl(url);
        }

        url = HaloUtils.initializeUrlIfBlank(url);

        InputConverter.super.update(post);

        // Crypt password
        if (StringUtils.isNotBlank(password)) {
            post.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        }
    }
}
