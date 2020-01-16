package run.halo.app.handler.migrate.support.vo;

import lombok.Data;
import run.halo.app.model.entity.BaseComment;
import run.halo.app.model.entity.BasePost;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Tag;

import java.util.List;

/**
 * @author guqing
 * @date 2020-01-18 16:52
 */
@Data
public class PostVO {
    private BasePost basePost;
    private List<Tag> tags;
    private List<Category> categories;
    private List<BaseComment> comments;
}
