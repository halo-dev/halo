package run.halo.app.model.params;

import lombok.Data;

/**
 * @author Raremaa
 * @date 2020/12/25 11:22 上午
 */
@Data
public class PostMarkdownParam {

    /**
     * true if need frontMatter
     * default false
     */
    private Boolean needFrontMatter;
}
