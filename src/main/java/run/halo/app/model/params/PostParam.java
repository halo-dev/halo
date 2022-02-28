package run.halo.app.model.params;

import java.util.HashSet;
import java.util.Set;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.PostMeta;
import run.halo.app.model.enums.PostEditorType;
import run.halo.app.model.support.NotAllowSpaceOnly;
import run.halo.app.utils.SlugUtils;

/**
 * Post param.
 *
 * @author johnniang
 * @author ryanwang
 * @author guqing
 * @date 2019-03-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostParam extends BasePostParam implements InputConverter<Post> {

    private Set<Integer> tagIds;

    private Set<Integer> categoryIds;

    private Set<PostMetaParam> metas;

    @Override
    @NotBlank(message = "文章标题不能为空")
    @Size(max = 100, message = "文章标题的字符长度不能超过 {max}")
    public String getTitle() {
        return super.getTitle();
    }

    @Override
    @Size(max = 255, message = "文章别名的字符长度不能超过 {max}")
    public String getSlug() {
        return super.getSlug();
    }

    @Override
    @Size(max = 255, message = "文章密码的字符长度不能超过 {max}")
    @NotAllowSpaceOnly(message = "密码开头和结尾不能包含空字符串")
    public String getPassword() {
        return super.getPassword();
    }

    public Set<PostMeta> getPostMetas() {
        Set<PostMeta> postMetaSet = new HashSet<>();
        if (CollectionUtils.isEmpty(metas)) {
            return postMetaSet;
        }

        for (PostMetaParam postMetaParam : metas) {
            PostMeta postMeta = postMetaParam.convertTo();
            postMetaSet.add(postMeta);
        }
        return postMetaSet;
    }

    @Override
    public Post convertTo() {
        slug = StringUtils.isBlank(slug) ? SlugUtils.slug(title) : SlugUtils.slug(slug);

        if (null == thumbnail) {
            thumbnail = "";
        }

        if (null == editorType) {
            editorType = PostEditorType.MARKDOWN;
        }

        Post post = InputConverter.super.convertTo();
        populateContent(post);
        return post;
    }

    @Override
    public void update(Post post) {
        slug = StringUtils.isBlank(slug) ? SlugUtils.slug(title) : SlugUtils.slug(slug);

        if (null == thumbnail) {
            thumbnail = "";
        }

        if (null == editorType) {
            editorType = PostEditorType.MARKDOWN;
        }
        populateContent(post);
        InputConverter.super.update(post);
    }
}
