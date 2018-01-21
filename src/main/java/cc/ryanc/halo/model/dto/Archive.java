package cc.ryanc.halo.model.dto;

import cc.ryanc.halo.model.domain.Post;
import lombok.Data;

import java.util.List;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * description :
 * @date : 2018/1/20
 */
@Data
public class Archive {

    private String year;

    private String month;

    private String count;

    private List<Post> posts;
}
