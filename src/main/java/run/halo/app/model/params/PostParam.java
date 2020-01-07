package run.halo.app.model.params;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.PostMeta;
import run.halo.app.model.enums.PostCreateFrom;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.utils.SlugUtils;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Post param.
 *
 * @author johnniang
 * @author ryanwang
 * @author guqing
 * @date 2019-03-21
 */
@Data
public class PostParam implements InputConverter<Post> {

    @NotBlank(message = "文章标题不能为空")
    @Size(max = 100, message = "文章标题的字符长度不能超过 {max}")
    private String title;

    private PostStatus status = PostStatus.DRAFT;

    private String url;

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

    private Set<PostMetaParam> postMetas;

    @Override
    public Post convertTo() {
        url = StringUtils.isBlank(url) ? SlugUtils.slug(title) : SlugUtils.slug(url);

        if (null == thumbnail) {
            thumbnail = "";
        }

        return InputConverter.super.convertTo();
    }

    @Override
    public void update(Post post) {
        url = StringUtils.isBlank(url) ? SlugUtils.slug(title) : SlugUtils.slug(url);

        if (null == thumbnail) {
            thumbnail = "";
        }

        InputConverter.super.update(post);
    }

    public Set<PostMeta> getPostMetas() {
        Set<PostMeta> postMetaSet = new HashSet<>();
        if (CollectionUtils.isEmpty(postMetas)) {
            return postMetaSet;
        }

        for (PostMetaParam postMetaParam : postMetas) {
            PostMeta postMeta = postMetaParam.convertTo();
            postMetaSet.add(postMeta);
        }
        return postMetaSet;
    }
}
