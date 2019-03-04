package cc.ryanc.halo.model.params;

import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.dto.base.AbstractInputConverter;
import cc.ryanc.halo.utils.MarkdownUtils;
import lombok.Data;

/**
 * Parameter of journal.
 *
 * @author : RYAN0UP
 * @date : 2019/03/04
 */
@Data
public class JournalParam extends AbstractInputConverter<Post> {

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 发布来源
     */
    private String source;

    @Override
    public Post convertTo() {
        Post post = super.convertTo();
        post.setPostContentMd(content);
        post.setPostContent(MarkdownUtils.renderMarkdown(content));
        return post;
    }
}
