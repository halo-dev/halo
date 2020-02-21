package run.halo.app.handler.migrate.support.wordpress;

import lombok.Data;

/**
 * WordPress导出的xml数据中对应的rss节点下的子节点channel将会被映射到该类属性
 * 如果不写这个Rss类包装想要的Channel,会无法解析到想要的结果
 *
 * @author guqing
 * @date 2020-01-17 00:45
 */
@Data
public class Rss {
    private Channel channel;
}
