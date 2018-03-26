package cc.ryanc.halo.model.dto;

import cc.ryanc.halo.model.domain.Post;
import lombok.Data;

import java.util.List;

/**
 * @author : RYAN0UP
 * @date : 2018/1/20
 * @version : 1.0
 * description : 文章归档数据
 */
@Data
public class Archive {

    /**
     * 年份
     */
    private String year;

    /**
     * 月份
     */
    private String month;

    /**
     * 对应的文章数
     */
    private String count;

    /**
     * 对应的文章
     */
    private List<Post> posts;
}
