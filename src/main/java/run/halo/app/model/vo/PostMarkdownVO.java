package run.halo.app.model.vo;

import lombok.Data;
import lombok.ToString;

/**
 * Markdown export VO
 *
 * @author Raremaa
 * @date 2020/12/25 9:14 上午
 */
@Data
@ToString
public class PostMarkdownVO {

    private String title;

    private String slug;

    private String originalContent;

    private String frontMatter;
}
