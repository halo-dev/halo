package run.halo.app.model.params;

import java.util.Date;
import java.util.Objects;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import lombok.Data;
import org.springframework.util.Assert;
import run.halo.app.model.entity.BasePost;
import run.halo.app.model.entity.Content;
import run.halo.app.model.enums.PostEditorType;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.utils.MarkdownUtils;

/**
 * @author guqing
 * @date 2022-02-21
 */
@Data
public abstract class BasePostParam {

    protected String title;

    protected PostStatus status = PostStatus.DRAFT;

    protected String slug;

    protected String password;

    protected PostEditorType editorType;

    protected String originalContent;

    protected String summary;

    @Size(max = 1023, message = "封面图链接的字符长度不能超过 {max}")
    protected String thumbnail;

    protected Boolean disallowComment = false;

    @Size(max = 255, message = "模版字符长度不能超过 {max}")
    protected String template;

    @Min(value = 0, message = "排序字段值不能小于 {value}")
    protected Integer topPriority = 0;

    protected Date createTime;

    protected String metaKeywords;

    protected String metaDescription;

    protected Boolean serverSideMarkdownRender = false;

    protected <T extends BasePost> void populateContent(T post) {
        Assert.notNull(post, "The post must not be null.");

        Content postContent = new Content();
        postContent.setOriginalContent(originalContent);

        if (Objects.equals(serverSideMarkdownRender, true)
            && PostEditorType.MARKDOWN.equals(editorType)) {
            postContent.setContent(MarkdownUtils.renderHtml(originalContent));
        } else {
            postContent.setContent(originalContent);
        }
        post.setContent(Content.PatchedContent.of(postContent));
    }
}
