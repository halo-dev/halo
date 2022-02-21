package run.halo.app.model.params;

import java.util.Objects;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import run.halo.app.model.enums.PostEditorType;
import run.halo.app.utils.MarkdownUtils;

/**
 * Post content param.
 *
 * @author johnniang
 */
@Data
public class PostContentParam {

    private String content;

    private String originalContent;

    /**
     * if {@code true}, it means is that do not let the back-end render the original content
     * because the content has been rendered, and you only need to store the original content.
     * otherwise, need server-side rendering.
     */
    private Boolean keepRaw = false;

    /**
     * Decide on post content based on {@link PostEditorType} and serverSideMarkdownRender.
     *
     * @param editorType edit type to use
     * @return formatted content of post.
     */
    public String decideContentBy(PostEditorType editorType) {
        String originalContentToUse = StringUtils.defaultString(originalContent, "");
        String result;
        if (Objects.equals(keepRaw, false)
            && PostEditorType.MARKDOWN.equals(editorType)) {
            result = MarkdownUtils.renderHtml(originalContentToUse);
        } else if (PostEditorType.RICHTEXT.equals(editorType)) {
            result = originalContentToUse;
        } else {
            result = this.content;
        }
        return result;
    }
}
