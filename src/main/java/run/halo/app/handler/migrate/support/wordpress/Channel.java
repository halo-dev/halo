package run.halo.app.handler.migrate.support.wordpress;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * WordPress导出的xml中对应的channel节点下的子节点将被映射为该类的属性
 * </p>
 *
 * @author guqing
 * @date 2019-11-17 13:57
 */
@Data
public class Channel {
    @JSONField(name = "title")
    private String title;
    private String link;
    private String description;
    private String pubDate;
    private String language;
    private String author;

    @JSONField(name = "item")
    private List<Item> items;
}
