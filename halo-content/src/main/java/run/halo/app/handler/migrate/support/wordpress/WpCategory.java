package run.halo.app.handler.migrate.support.wordpress;

import lombok.Data;
import run.halo.app.handler.migrate.utils.PropertyMappingTo;
import run.halo.app.model.entity.PostCategory;

/**
 * WordPress导出的xml数据中对应的category的子节点将会被映射到该类属性,
 * 最终会被转换为文章分类{@link PostCategory}
 *
 * @author guqing
 * @date 2020-01-18 16:09
 */
@Data
public class WpCategory {
    private String domain;

    @PropertyMappingTo("slugName")
    private String nicename;

    @PropertyMappingTo("name")
    private String content;
}
